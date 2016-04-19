// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

import com.microsoft.alm.authentication.BaseVsoAuthentication;
import com.microsoft.alm.authentication.BasicAuthentication;
import com.microsoft.alm.authentication.Configuration;
import com.microsoft.alm.authentication.Credential;
import com.microsoft.alm.authentication.DeviceFlowResponse;
import com.microsoft.alm.authentication.IAuthentication;
import com.microsoft.alm.authentication.ISecureStore;
import com.microsoft.alm.authentication.ITokenStore;
import com.microsoft.alm.authentication.IVsoAadAuthentication;
import com.microsoft.alm.authentication.IVsoMsaAuthentication;
import com.microsoft.alm.authentication.KeychainSecurityCliStore;
import com.microsoft.alm.authentication.SecretStore;
import com.microsoft.alm.authentication.VsoAadAuthentication;
import com.microsoft.alm.authentication.VsoMsaAuthentication;
import com.microsoft.alm.authentication.VsoTokenScope;
import com.microsoft.alm.authentication.Where;
import com.microsoft.alm.helpers.Action;
import com.microsoft.alm.helpers.Debug;
import com.microsoft.alm.helpers.Environment;
import com.microsoft.alm.helpers.Func;
import com.microsoft.alm.helpers.Guid;
import com.microsoft.alm.helpers.IOHelper;
import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.helpers.Path;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.helpers.Trace;
import com.microsoft.alm.helpers.UriHelper;
import com.microsoft.alm.oauth2.useragent.Provider;
import com.microsoft.alm.oauth2.useragent.Version;
import com.microsoft.alm.oauth2.useragent.subprocess.DefaultProcessFactory;
import com.microsoft.alm.oauth2.useragent.subprocess.ProcessCoordinator;
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcess;
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcessFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public class Program
{
    private static final String ConfigPrefix = "credential";
    private static final String SecretsNamespace = "git";
    private static final String ProgramFolderName = "git-credential-manager";
    private static final VsoTokenScope VsoCredentialScope = VsoTokenScope.CodeWrite;
    private static final String AbortAuthenticationProcessResponse = "quit=true";
    private static final String CredentialHelperSection = "credential.helper";
    private static final String CredentialHelperValueRegex = "git-credential-manager-[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)?.jar";
    private static final String CanFallbackToInsecureStore = "canFallBackToInsecureStore";
    private static final DefaultFileChecker DefaultFileCheckerSingleton = new DefaultFileChecker();

    private final InputStream standardIn;
    private final PrintStream standardOut;
    private final IComponentFactory componentFactory;
    private static final Action<DeviceFlowResponse> DEVICE_FLOW_CALLBACK = new Action<DeviceFlowResponse>()
    {
        @Override public void call(final DeviceFlowResponse deviceFlowResponse)
        {
            System.err.println("------------------------------------");
            System.err.println("OAuth 2.0 Device Flow authentication");
            System.err.println("------------------------------------");
            System.err.println("To complete the authentication process, please open a web browser and visit the following URI:");
            System.err.println(deviceFlowResponse.getVerificationUri());
            System.err.println("When prompted, enter the following code:");
            System.err.println(deviceFlowResponse.getUserCode());
            System.err.println("Once authenticated and authorized, execution will continue.");
        }
    };

    // http://stackoverflow.com/a/6773868/
    static String getVersion()
    {
        if (_version == null)
        {
            _version = Program.class.getPackage().getImplementationVersion();
        }
        return _version;
    }

    private static String _version;

    static String getTitle()
    {
        if (_title == null)
        {
            _title = Program.class.getPackage().getImplementationTitle();
        }
        return _title;
    }

    private static String _title;

    public static void main(final String[] args)
    {
        try
        {
            enableDebugTrace();
            final Program program = new Program(System.in, System.out, new ComponentFactory());

            program.innerMain(args);
        }
        catch (final Throwable throwable)
        {
            if (Debug.IsDebug)
            {
                System.err.println("Fatal error encountered.  Details:");
                throwable.printStackTrace(System.err);
            }
            else
            {
                System.err.println("Fatal: " + throwable.getClass().getName() + " encountered.  Details:");
                System.err.println(throwable.getMessage());
            }
            logEvent(throwable.getMessage(), "EventLogEntryType.Error");
            // notice the lack of a new line; Git needs it that way
            System.out.print(AbortAuthenticationProcessResponse);
        }

        Trace.flush();
    }

    static File determineParentFolder()
    {
        return findFirstValidFolder(
            Environment.SpecialFolder.LocalApplicationData,
            Environment.SpecialFolder.ApplicationData,
            Environment.SpecialFolder.UserProfile);
    }

    static File findFirstValidFolder(final Environment.SpecialFolder... candidates)
    {
        for (final Environment.SpecialFolder candidate : candidates)
        {
            final String path = Environment.getFolderPath(candidate);
            if (path == null)
                continue;
            final File result = new File(path);
            if (result.isDirectory())
            {
                return result;
            }
        }
        final String path = System.getenv("HOME");
        final File result = new File(path);
        return result;
    }

    void innerMain(String[] args) throws Exception
    {
        if (args.length == 0 || args[0].contains("?"))
        {
            printHelpMessage();
            return;
        }

        // list of arg => method associations (case-insensitive)
        final Map<String, Callable<Void>> actions = new TreeMap<String, Callable<Void>>(String.CASE_INSENSITIVE_ORDER);
        actions.put("approve", Store);
        actions.put("erase", Erase);
        actions.put("fill", Get);
        actions.put("get", Get);
        actions.put("reject", Erase);
        actions.put("store", Store);
        actions.put("version", PrintVersion);
        actions.put("install", Install);
        actions.put("uninstall", Uninstall);

        for (final String arg : args)
        {
            if (actions.containsKey(arg))
            {
                actions.get(arg).call();
            }
        }
    }

    public Program(final InputStream standardIn, final PrintStream standardOut, final IComponentFactory componentFactory)
    {
        this.standardIn = standardIn;
        this.standardOut = standardOut;
        this.componentFactory = componentFactory;
    }

    private void printHelpMessage()
    {
        Trace.writeLine("Program::printHelpMessage");

        standardOut.println("usage: git credential <command> [<args>]");
        standardOut.println();
        standardOut.println("   authority          Defines the type of authentication to be used.");
        standardOut.println("                      Supports Auto, Basic, AAD, MSA, and Integrated.");
        standardOut.println("                      Default is Auto.");
        standardOut.println();
        standardOut.println("      `git config --global credential.microsoft.visualstudio.com.authority AAD`");
        standardOut.println();
        standardOut.println("   eraseosxkeychain   Enables a workaround when running on Mac OS X");
        standardOut.println("                      and using 'Apple Git' (which includes the osxkeychain");
        standardOut.println("                      credential helper, hardcoded before all other helpers).");
        standardOut.println("                      The problem is osxkeychain may return expired or");
        standardOut.println("                      revoked credentials, aborting the Git operation.");
        standardOut.println("                      The workaround is to preemptively erase from osxkeychain");
        standardOut.println("                      any Git credentials that can be refreshed or re-acquired");
        standardOut.println("                      by this credential helper.");
        standardOut.println("                      Defaults to FALSE. Ignored by Basic authority.");
        standardOut.println("                      Does nothing if Apple Git on Mac OS X isn't detected.");
        standardOut.println();
        standardOut.println("      `git config --global credential.microsoft.visualstudio.com.eraseosxkeychain false`");
        standardOut.println();
        standardOut.println("   interactive        Specifies if user can be prompted for credentials or not.");
        standardOut.println("                      Supports Auto, Always, or Never. Defaults to Auto.");
        standardOut.println("                      Only used by AAD and MSA authority.");
        standardOut.println();
        standardOut.println("      `git config --global credential.microsoft.visualstudio.com.interactive never`");
        standardOut.println();
        standardOut.println("   validate           Causes validation of credentials before supplying them");
        standardOut.println("                      to Git. Invalid credentials get a refresh attempt");
        standardOut.println("                      before failing. Incurs some minor overhead.");
        standardOut.println("                      Defaults to TRUE. Ignored by Basic authority.");
        standardOut.println();
        standardOut.println("      `git config --global credential.microsoft.visualstudio.com.validate false`");
        standardOut.println();
        standardOut.println("   writelog           Enables trace logging of all activities. Logs are written to");
        standardOut.println("                      the .git/ folder at the root of the repository.");
        standardOut.println("                      Defaults to FALSE.");
        standardOut.println();
        standardOut.println("      `git config --global credential.writelog true`");
        standardOut.println();
        standardOut.println("Sample Configuration:");
        standardOut.println("   [credential \"microsoft.visualstudio.com\"]");
        standardOut.println("       authority = AAD");
        standardOut.println("   [credential \"visualstudio.com\"]");
        standardOut.println("       authority = MSA");
        standardOut.println("   [credential]");
        standardOut.println("       helper = manager");
    }

    private final Callable<Void> Erase = new Callable<Void>()
    {
        @Override public Void call() throws IOException, URISyntaxException
        {
            erase();
            return null;
        }
    };
    private void erase() throws IOException, URISyntaxException
    {
        final AtomicReference<OperationArguments> operationArgumentsRef = new AtomicReference<OperationArguments>();
        final AtomicReference<IAuthentication> authenticationRef = new AtomicReference<IAuthentication>();
        initialize("erase", operationArgumentsRef, authenticationRef);
        erase(operationArgumentsRef.get(), authenticationRef.get());
    }
    public static void erase(final OperationArguments operationArguments, final IAuthentication authentication)
    {
        authentication.deleteCredentials(operationArguments.TargetUri);
    }

    private final Callable<Void> Get = new Callable<Void>()
    {
        @Override public Void call() throws IOException, URISyntaxException
        {
            get();
            return null;
        }
    };
    private void get() throws IOException, URISyntaxException
    {
        final AtomicReference<OperationArguments> operationArgumentsRef = new AtomicReference<OperationArguments>();
        final AtomicReference<IAuthentication> authenticationRef = new AtomicReference<IAuthentication>();
        initialize("get", operationArgumentsRef, authenticationRef);
        final String result = get(operationArgumentsRef.get(), authenticationRef.get(), DEVICE_FLOW_CALLBACK);
        standardOut.print(result);
    }
    public static String get(final OperationArguments operationArguments, final IAuthentication authentication, final Action<DeviceFlowResponse> deviceFlowCallback)
    {
        final String AuthFailureMessage = "Logon failed, aborting authentication process.";

        final AtomicReference<Credential> credentials = new AtomicReference<Credential>();

        switch (operationArguments.Authority)
        {
            default:
            case Basic:
                if (authentication.getCredentials(operationArguments.TargetUri, credentials))
                {
                    Trace.writeLine("   credentials found");
                    operationArguments.setCredentials(credentials.get());
                }
                break;

            case AzureDirectory:
                final IVsoAadAuthentication aadAuth = (IVsoAadAuthentication) authentication;

                // attempt to get cached creds -> refresh creds -> non-interactive logon -> interactive logon
                // note that AAD "credentials" are always scoped access tokens
                if (
                    (operationArguments.Interactivity != Interactivity.Always
                        && aadAuth.getCredentials(operationArguments.TargetUri, credentials)
                        && (!operationArguments.ValidateCredentials
                            || aadAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                    || (operationArguments.Interactivity != Interactivity.Always
                        && aadAuth.refreshCredentials(operationArguments.TargetUri, true)
                        && aadAuth.getCredentials(operationArguments.TargetUri, credentials)
                        && (!operationArguments.ValidateCredentials
                            || aadAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
//                        || (operationArguments.Interactivity != Interactivity.Always
//                            && aadAuth.noninteractiveLogon(operationArguments.TargetUri, true)
//                            && aadAuth.getCredentials(operationArguments.TargetUri, credentials)
//                            && (!operationArguments.ValidateCredentials
//                                || aadAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                    || (operationArguments.Interactivity != Interactivity.Never
                        && aadAuth.interactiveLogon(operationArguments.TargetUri, true)
                        && aadAuth.getCredentials(operationArguments.TargetUri, credentials)
                        && (!operationArguments.ValidateCredentials
                            || aadAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                    || (operationArguments.Interactivity != Interactivity.Never
                        && aadAuth.deviceLogon(operationArguments.TargetUri, true, deviceFlowCallback)
                        && aadAuth.getCredentials(operationArguments.TargetUri, credentials)
                        && (!operationArguments.ValidateCredentials
                            || aadAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                )
                {
                    Trace.writeLine("   credentials found");
                    operationArguments.setCredentials(credentials.get());
                    logEvent("Azure Directory credentials for " + operationArguments.TargetUri + " successfully retrieved.", "SuccessAudit");
                }
                else
                {
                    System.err.println(AuthFailureMessage);
                    logEvent("Failed to retrieve Azure Directory credentials for " + operationArguments.TargetUri + ".", "FailureAudit");
                    return AbortAuthenticationProcessResponse;
                }

                break;

            case MicrosoftAccount:
                final IVsoMsaAuthentication msaAuth = (IVsoMsaAuthentication) authentication;

                // attempt to get cached creds -> refresh creds -> interactive logon
                // note that MSA "credentials" are always scoped access tokens
                if (
                    (operationArguments.Interactivity != Interactivity.Always
                        && msaAuth.getCredentials(operationArguments.TargetUri, credentials)
                        && (!operationArguments.ValidateCredentials
                            || msaAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                    || (operationArguments.Interactivity != Interactivity.Always
                        && msaAuth.refreshCredentials(operationArguments.TargetUri, true)
                        && msaAuth.getCredentials(operationArguments.TargetUri, credentials)
                        && (!operationArguments.ValidateCredentials
                            || msaAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                    || (operationArguments.Interactivity != Interactivity.Never
                        && msaAuth.interactiveLogon(operationArguments.TargetUri, true)
                        && msaAuth.getCredentials(operationArguments.TargetUri, credentials)
                        && (!operationArguments.ValidateCredentials
                            || msaAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                    || (operationArguments.Interactivity != Interactivity.Never
                        && msaAuth.deviceLogon(operationArguments.TargetUri, true, deviceFlowCallback)
                        && msaAuth.getCredentials(operationArguments.TargetUri, credentials)
                        && (!operationArguments.ValidateCredentials
                            || msaAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                )
                {
                    Trace.writeLine("   credentials found");
                    operationArguments.setCredentials(credentials.get());
                    logEvent("Microsoft Live credentials for " + operationArguments.TargetUri + " successfully retrieved.", "SuccessAudit");
                }
                else
                {
                    System.err.println(AuthFailureMessage);
                    logEvent("Failed to retrieve Microsoft Live credentials for " + operationArguments.TargetUri + ".", "FailureAudit");
                    return AbortAuthenticationProcessResponse;
                }

                break;

            case GitHub:
                throw new NotImplementedException(449515);

            case Integrated:
                credentials.set(new Credential(StringHelper.Empty, StringHelper.Empty));
                operationArguments.setCredentials(credentials.get());
                break;
        }

        return operationArguments.toString();
    }

    private final Callable<Void> Store = new Callable<Void>()
    {
        @Override public Void call() throws IOException, URISyntaxException
        {
            store();
            return null;
        }
    };
    private void store() throws IOException, URISyntaxException
    {
        final AtomicReference<OperationArguments> operationArgumentsRef = new AtomicReference<OperationArguments>();
        final AtomicReference<IAuthentication> authenticationRef = new AtomicReference<IAuthentication>();
        initialize("store", operationArgumentsRef, authenticationRef);

        final String osName = System.getProperty("os.name");
        final TestableProcessFactory processFactory = new DefaultProcessFactory();
        final String pathString = System.getenv("PATH");
        final String pathSeparator = File.pathSeparator;
        store(operationArgumentsRef.get(), authenticationRef.get(), osName, processFactory, DefaultFileCheckerSingleton, pathString, pathSeparator);
    }
    public static void store(final OperationArguments operationArguments, final IAuthentication authentication, final String osName, final TestableProcessFactory processFactory, final Func<File, Boolean> fileChecker, final String pathString, final String pathSeparator)
    {
        Debug.Assert(operationArguments.getUserName() != null, "The operationArguments.Username is null");

        final Credential credentials = new Credential(operationArguments.getUserName(), operationArguments.getPassword());
        if (authentication instanceof BasicAuthentication)
        {
            authentication.setCredentials(operationArguments.TargetUri, credentials);
        }
        else
        {
            if (operationArguments.EraseOsxKeyChain && Provider.isMac(osName))
            {
                final String gitResponse = fetchGitVersion(processFactory);
                if (gitResponse.contains("Apple Git-"))
                {
                    // check for the presence of git-credential-osxkeychain by scanning PATH
                    final File osxkeychainFile = findProgram(pathString, pathSeparator, "git-credential-osxkeychain", fileChecker);
                    if (osxkeychainFile != null)
                    {
                        // erase these credentials from osxkeychain
                        try
                        {
                            final String program = osxkeychainFile.getAbsolutePath();
                            final TestableProcess process = processFactory.create(program, "erase");
                            final ProcessCoordinator coordinator = new ProcessCoordinator(process);
                            coordinator.print(operationArguments.toString());
                            coordinator.waitFor();
                        }
                        catch (final IOException e)
                        {
                            throw new Error(e);
                        }
                        catch (final InterruptedException e)
                        {
                            throw new Error(e);
                        }
                    }
                }
            }
        }
    }

    private final Callable<Void> PrintVersion = new Callable<Void>()
    {
        @Override public Void call()
        {
            printVersion();
            return null;
        }
    };
    private void printVersion()
    {
        Trace.writeLine("Program::printVersion");

        standardOut.println(String.format("%1$s version %2$s", getTitle(), getVersion()));
    }

    private final Callable<Void> Install = new Callable<Void>()
    {
        @Override public Void call()
        {
            install();
            return null;
        }
    };
    private void install()
    {
        final String osName = System.getProperty("os.name");
        final String osVersion = System.getProperty("os.version");
        final List<Provider> providers = Provider.PROVIDERS;
        final TestableProcessFactory processFactory = new DefaultProcessFactory();
        install(osName, osVersion, standardOut, providers, processFactory);
    }

    static void install(final String osName, final String osVersion, final PrintStream standardOut, final List<Provider> providers, final TestableProcessFactory processFactory)
    {
        List<String> missedRequirements = new ArrayList<String>();
        missedRequirements.addAll(checkGitRequirements(processFactory));
        missedRequirements.addAll(checkOsRequirements(osName, osVersion));

        if (missedRequirements.isEmpty())
        {
            try
            {
                // TODO: 457304: Add option to configure for global or system
                final String configLocation = "global";
                uninstall(processFactory);
                configureGit(processFactory, configLocation);
            }
            catch (final IOException e)
            {
                throw new Error(e);
            }
            catch (final InterruptedException e)
            {
                throw new Error(e);
            }
        }
        else
        {
            standardOut.println("Installation failed due to the following unmet requirements:");
            for (String msg : missedRequirements)
            {
                standardOut.println(msg);
            }
            standardOut.println();
            standardOut.println("If you think we are excluding many users with one or more of these requirements, please let us know.");
        }
    }

    static void configureGit(final TestableProcessFactory processFactory, final String configLocation) throws IOException, InterruptedException
    {
        final URL resourceURL = Program.class.getResource("");
        final String javaHome = System.getProperty("java.home");
        final File javaExecutable = new File(javaHome, "bin/java");
        final String pathToJava = javaExecutable.getAbsolutePath();
        final String pathToJar = determinePathToJar(resourceURL);
        final boolean isDebug = Debug.IsDebug;

        configureGit(processFactory, configLocation, pathToJava, pathToJar, isDebug);
    }

    static void configureGit(final TestableProcessFactory processFactory, final String configLocation, final String pathToJava, final String pathToJar, final boolean isDebug) throws IOException, InterruptedException
    {
        final StringBuilder sb = new StringBuilder();
        // escape spaces (if any) in paths to java and path to JAR
        // i.e. !/usr/bin/jre\ 1.6/bin/java -Ddebug=false -jar /home/example/with\ spaces/gcm.jar
        sb.append("!").append(escapeSpaces(pathToJava));
        sb.append(" -Ddebug=").append(isDebug);
        sb.append(" -Djava.net.useSystemProxies=true");
        sb.append(" -jar ").append(escapeSpaces(pathToJar));
        final String gcmCommandLine = sb.toString();

        final String[] command =
        {
            "git",
            "config",
            "--" + configLocation,
            "--add",
            CredentialHelperSection,
            gcmCommandLine,
        };
        final TestableProcess process = processFactory.create(command);
        final ProcessCoordinator coordinator = new ProcessCoordinator(process);
        final int exitCode = coordinator.waitFor();
        checkGitConfigExitCode(configLocation, exitCode);
    }

    static String escapeSpaces(final String input)
    {
        return input.replace(" ", "\\ ");
    }

    private final Callable<Void> Uninstall = new Callable<Void>()
    {
        @Override public Void call()
        {
            uninstall();
            return null;
        }
    };
    private void uninstall()
    {
        final TestableProcessFactory processFactory = new DefaultProcessFactory();
        uninstall(processFactory);
    }

    static void uninstall(final TestableProcessFactory processFactory)
    {
        try
        {
            final String configLocation = "global";
            // TODO: 457304: unconfigure from both global and system (if we can!), to be sure
            if (isGitConfigured(processFactory, configLocation))
            {
                unconfigureGit(processFactory, configLocation);
            }
        }
        catch (final IOException e)
        {
            throw new Error(e);
        }
        catch (final InterruptedException e)
        {
            throw new Error(e);
        }
    }

    static boolean isGitConfigured(final TestableProcessFactory processFactory, final String configLocation) throws IOException, InterruptedException
    {
        final String[] command =
            {
                "git",
                "config",
                "--" + configLocation,
                "--get",
                CredentialHelperSection,
                CredentialHelperValueRegex,
            };
        final TestableProcess process = processFactory.create(command);
        final ProcessCoordinator coordinator = new ProcessCoordinator(process);
        coordinator.waitFor();
        final String stdOut = coordinator.getStdOut();
        final boolean result = stdOut.length() > 0;
        return result;
    }

    static void unconfigureGit(final TestableProcessFactory processFactory, final String configLocation) throws IOException, InterruptedException
    {
        final String[] command =
            {
                "git",
                "config",
                "--" + configLocation,
                "--unset",
                CredentialHelperSection,
                CredentialHelperValueRegex,
            };
        final TestableProcess process = processFactory.create(command);
        final ProcessCoordinator coordinator = new ProcessCoordinator(process);
        final int exitCode = coordinator.waitFor();
        checkGitConfigExitCode(configLocation, exitCode);
    }

    static void checkGitConfigExitCode(final String configLocation, final int exitCode)
    {
        String message;
        switch (exitCode)
        {
            case 0:
                message = null;
                break;
            case 3:
                message = "The '" + configLocation + "' Git config file is invalid.";
                break;
            case 4:
                message = "Can not write to the '" + configLocation + "' Git config file.";
                break;
            default:
                message = "Unexpected exit code '" + exitCode + "' received from `git config`.";
                break;
        }
        if (message != null)
        {
            throw new Error(message);
        }
    }

    /**
     * Determines the path of the JAR, given a URL to a resource inside the current JAR.
     *
     */
    static String determinePathToJar(final URL resourceURL)
    {
        final String packageName = Program.class.getPackage().getName();
        final String resourcePath = resourceURL.getPath();
        final String decodedResourcePath;
        try
        {
            decodedResourcePath = URLDecoder.decode(resourcePath, UriHelper.UTF_8);
        }
        catch (final UnsupportedEncodingException e)
        {
            throw new Error(e);
        }
        final String packagePath = packageName.replace(".", "/");
        final String resourceSuffix = "!/" + packagePath + "/";
        String jarPath = decodedResourcePath.replace(resourceSuffix, "");
        jarPath = jarPath.replace("file:", "");
        return jarPath;
    }

    /**
     * Checks if git version can be found and if it is the correct version
     *
     * @return if git requirements are met
     */
    static List<String> checkGitRequirements(final TestableProcessFactory processFactory)
    {
        final String trimmedResponse = fetchGitVersion(processFactory);
        return isValidGitVersion(trimmedResponse);
    }

    static String fetchGitVersion(final TestableProcessFactory processFactory)
    {
        try
        {
            // finding git version via commandline
            final TestableProcess gitProcess = processFactory.create("git", "--version");
            final ProcessCoordinator coordinator = new ProcessCoordinator(gitProcess);
            coordinator.waitFor();
            final String gitResponse = coordinator.getStdOut();
            return gitResponse.trim();
        }
        catch (final IOException e)
        {
            throw new Error(e);
        }
        catch (final InterruptedException e)
        {
            throw new Error(e);
        }
    }

    private static class DefaultFileChecker implements Func<File, Boolean>
    {
        @Override public Boolean call(final File file)
        {
            return file.isFile();
        }
    }

    static File findProgram(final String pathString, final String pathSeparator, final String executableName, final Func<File, Boolean> fileChecker)
    {
        final String[] partArray = pathString.split(pathSeparator);
        final List<String> parts = Arrays.asList(partArray);
        return findProgram(parts, executableName, fileChecker);
    }

    static File findProgram(final List<String> directories, final String executableName, final Func<File, Boolean> fileChecker)
    {
        for (final String directoryString : directories)
        {
            final File directory = new File(directoryString);
            final File executableFile = new File(directory, executableName);
            if (fileChecker.call(executableFile))
            {
                return executableFile;
            }
        }
        return null;
    }

    /**
     * Parses git version response for major and minor version and checks if it's 1.9 or above
     *
     * @param gitResponse the output from 'git --version'
     * @return if the git version meets the requirement
     */
    protected static List<String> isValidGitVersion(final String gitResponse)
    {
        Trace.writeLine("Program::isValidGitVersion");
        Trace.writeLine("  gitResponse:" + gitResponse);
        final String GitNotFound = "Git is a requirement for installation and cannot be found. Please check that Git is installed and is added to your PATH";
        final List<String> result = new ArrayList<String>();
        // if git responded with a version then parse it for the version number
        if (gitResponse != null)
        {
            // TODO: 450002: Detect "Apple Git" and warn the user
            // git version numbers are in the form of x.y.z and we only need x.y to ensure the requirements are met
            Version version = null;
            try
            {
                version = Version.parseVersion(gitResponse);
            }
            catch (final IllegalArgumentException ignored)
            {
                Trace.writeLine("  " + ignored.getMessage());
                result.add(GitNotFound);
            }
            if (version != null)
            {
                if (version.getMajor() < 1
                    || (version.getMajor() == 1 && version.getMinor() < 9))
                {
                    result.add("Git version " + version.getMajor() + "." + version.getMinor() + " was found but version 1.9 or above is required.");
                }
            }
        }
        else
        {
            result.add(GitNotFound);
        }
        return result;
    }

    /**
     * Checks if the OS meets the requirements to run installation
     *
     * @param osName the name of the operating system, as retrieved from the os.name property.
     * @param osVersion the version of the operating system, as retrieved from the os.version property.
     * @return a list of strings representing unmet requirements.
     */
    protected static List<String> checkOsRequirements(final String osName, final String osVersion)
    {
        final ArrayList<String> result = new ArrayList<String>();

        if (Provider.isMac(osName))
        {
            final Version version = Version.parseVersion(osVersion);
            final String badVersionMessage = "The version of Mac OS X running is " + version.getMajor() + "." + version.getMinor() + "." + version.getPatch() +
                    " which does not meet the minimum version of 10.9.5 needed for installation. Please upgrade to Mac OS X 10.9.5 or above to proceed.";
            if (version.getMajor() < 10)
            {
                result.add(badVersionMessage);
            }
            else if (version.getMajor() == 10)
            {
                if (version.getMinor() < 9)
                {
                    result.add(badVersionMessage);
                }
                else if (version.getMinor() == 9)
                {
                    if (version.getPatch() < 5)
                    {
                        result.add(badVersionMessage);
                    }
                }
            }
        }
        else if (Provider.isLinux(osName))
        {
            // TODO: check for supported major distributions and versions (Ubuntu 14+, Fedora 22+, etc.)
        }
        else if (Provider.isWindows(osName))
        {
            result.add("It looks like you are running on Windows, please consider using the Git Credential Manager for Windows: https://github.com/Microsoft/Git-Credential-Manager-for-Windows");
        }
        else
        {
            result.add("The Git Credential Manager for Mac and Linux is only supported on, well, Mac OS X and Linux. The operating system detected is " + osName + ", which is not supported.");
        }
        return result;
    }

    private void initialize(
        final String methodName,
        final AtomicReference<OperationArguments> operationArgumentsRef,
        final AtomicReference<IAuthentication> authenticationRef
    ) throws IOException, URISyntaxException
    {
        // parse the operations arguments from stdin (this is how git sends commands)
        // see: https://www.kernel.org/pub/software/scm/git/docs/technical/api-credentials.html
        // see: https://www.kernel.org/pub/software/scm/git/docs/git-credential.html
        final OperationArguments operationArguments;
        final BufferedReader reader = new BufferedReader(new InputStreamReader(standardIn));
        try
        {
            operationArguments = new OperationArguments(reader);
        }
        finally
        {
            IOHelper.closeQuietly(reader);
        }

        Debug.Assert(operationArguments.TargetUri != null, "The operationArguments.TargetUri is null");

        final Configuration config = componentFactory.createConfiguration();
        loadOperationArguments(operationArguments, config);
        enableTraceLogging(operationArguments);

        Trace.writeLine("Program::" + methodName);
        Trace.writeLine("   targetUri = " + operationArguments.TargetUri);

        final ISecureStore secureStore = componentFactory.createSecureStore(operationArguments);
        final IAuthentication authentication = componentFactory.createAuthentication(operationArguments, secureStore);

        operationArgumentsRef.set(operationArguments);
        authenticationRef.set(authentication);
    }

    static IAuthentication createAuthentication(final OperationArguments operationArguments, final ISecureStore secureStore)
    {
        Debug.Assert(operationArguments != null, "The operationArguments is null");

        Trace.writeLine("Program::createAuthentication");

        final SecretStore secrets = new SecretStore(secureStore, SecretsNamespace);
        final AtomicReference<IAuthentication> authorityRef = new AtomicReference<IAuthentication>();
        final ITokenStore adaRefreshTokenStore = null;

        if (operationArguments.Authority == AuthorityType.Auto)
        {
            Trace.writeLine("   detecting authority type");

            // detect the authority
            if (BaseVsoAuthentication.getAuthentication(operationArguments.TargetUri,
                    VsoCredentialScope,
                    secrets,
                    adaRefreshTokenStore,
                    authorityRef)
                    /* TODO: 449515: add GitHub support
                    || GithubAuthentication.GetAuthentication(operationArguments.TargetUri,
                    GithubCredentialScope,
                    secrets,
                    authorityRef)*/)
            {
                // set the authority type based on the returned value
                if (authorityRef.get() instanceof VsoMsaAuthentication)
                {
                    operationArguments.Authority = AuthorityType.MicrosoftAccount;
                }
                else if (authorityRef.get() instanceof VsoAadAuthentication)
                {
                    operationArguments.Authority = AuthorityType.AzureDirectory;
                }
                /* TODO: 449515: add GitHub support
                else if (authorityRef instanceof GithubAuthentication)
                {
                    operationArguments.Authority = AuthorityType.GitHub;
                }
                */
            }
            else
            {
                operationArguments.Authority = AuthorityType.Basic;
            }
        }

        switch (operationArguments.Authority)
        {
            case AzureDirectory:
                Trace.writeLine("   authority is Azure Directory");

                // return the allocated authority or a generic AAD backed VSO authentication object
                return authorityRef.get() != null ? authorityRef.get() : new VsoAadAuthentication(Guid.Empty, VsoCredentialScope, secrets, adaRefreshTokenStore);

            case Basic:
            default:
                Trace.writeLine("   authority is basic");

                // return a generic username + password authentication object
                return authorityRef.get() != null ? authorityRef.get() : new BasicAuthentication(secrets);

            /* TODO: 449515: add GitHub support
            case GitHub:
                Trace.writeLine("    authority it GitHub");

                // return a GitHub authentication object
                return new GithubAuthentication(GithubCredentialScope, secrets);

            */
            case MicrosoftAccount:
                Trace.writeLine("   authority is Microsoft Live");

                // return the allocated authority or a generic MSA backed VSO authentication object
                return authorityRef.get() != null ? authorityRef.get() : new VsoMsaAuthentication(VsoCredentialScope, secrets, adaRefreshTokenStore);
        }
    }

    private static void loadOperationArguments(final OperationArguments operationArguments, final Configuration config) throws IOException
    {
        Debug.Assert(operationArguments != null, "The operationsArguments parameter is null.");

        Trace.writeLine("Program::loadOperationArguments");

        final AtomicReference<Configuration.Entry> entryRef = new AtomicReference<Configuration.Entry>();

        if (config.tryGetEntry(ConfigPrefix, operationArguments.TargetUri, "authority", entryRef))
        {
            Trace.writeLine("   authority = " + entryRef.get().Value);

            if ("MSA".equalsIgnoreCase(entryRef.get().Value)
                    || "Microsoft".equalsIgnoreCase(entryRef.get().Value)
                    || "MicrosoftAccount".equalsIgnoreCase(entryRef.get().Value)
                    || "Live".equalsIgnoreCase(entryRef.get().Value)
                    || "LiveConnect".equalsIgnoreCase(entryRef.get().Value)
                    || "LiveID".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.Authority = AuthorityType.MicrosoftAccount;
            }
            else if ("AAD".equalsIgnoreCase(entryRef.get().Value)
                    || "Azure".equalsIgnoreCase(entryRef.get().Value)
                    || "AzureDirectory".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.Authority = AuthorityType.AzureDirectory;
            }
            else if ("Integrated".equalsIgnoreCase(entryRef.get().Value)
                    || "NTLM".equalsIgnoreCase(entryRef.get().Value)
                    || "Kerberos".equalsIgnoreCase(entryRef.get().Value)
                    || "SSO".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.Authority = AuthorityType.Integrated;
            }
            else
            {
                operationArguments.Authority = AuthorityType.Basic;
            }
        }

        if (config.tryGetEntry(ConfigPrefix, operationArguments.TargetUri, "interactive", entryRef))
        {
            Trace.writeLine("   interactive = " + entryRef.get().Value);

            if ("always".equalsIgnoreCase(entryRef.get().Value)
                    || "true".equalsIgnoreCase(entryRef.get().Value)
                    || "force".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.Interactivity = Interactivity.Always;
            }
            else if ("never".equalsIgnoreCase(entryRef.get().Value)
                    || "false".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.Interactivity = Interactivity.Never;
            }
        }

        if (config.tryGetEntry(ConfigPrefix, operationArguments.TargetUri, "validate", entryRef))
        {
            Trace.writeLine("   validate = " + entryRef.get().Value);

            if ("true".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.ValidateCredentials = true;
            }
            else if ("false".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.ValidateCredentials = false;
            }
        }

        if (config.tryGetEntry(ConfigPrefix, operationArguments.TargetUri, "writelog", entryRef))
        {
            Trace.writeLine("   writelog = " + entryRef.get().Value);

            if ("true".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.WriteLog = true;
            }
            else if ("false".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.WriteLog = false;
            }
        }

        if (config.tryGetEntry(ConfigPrefix, operationArguments.TargetUri, "eraseosxkeychain", entryRef))
        {
            Trace.writeLine("   eraseosxkeychain = " + entryRef.get().Value);

            if ("true".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.EraseOsxKeyChain = true;
            }
            else if ("false".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.EraseOsxKeyChain = false;
            }
        }

        if (config.tryGetEntry(ConfigPrefix, operationArguments.TargetUri, CanFallbackToInsecureStore, entryRef))
        {
            Trace.writeLine("   " + CanFallbackToInsecureStore + " = " + entryRef.get().Value);

            if ("true".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.CanFallbackToInsecureStore = true;
            }
            else if ("false".equalsIgnoreCase(entryRef.get().Value))
            {
                operationArguments.CanFallbackToInsecureStore = false;
            }
        }
    }

    private static void logEvent(final String message, final Object eventType)
    {
        final String eventSource = "Git Credential Manager";

        /*** commented out due to UAC issues which require a proper installer to work around ***/

        //Trace.WriteLine("Program::LogEvent");

        //if (!EventLog.SourceExists(EventSource))
        //{
        //    EventLog.CreateEventSource(EventSource, "Application");

        //    Trace.WriteLine("   event source created");
        //}

        //EventLog.WriteEntry(EventSource, message, eventType);

        //Trace.WriteLine("   " + eventType + "event written");
    }

    private static void enableTraceLogging(final OperationArguments operationArguments) throws IOException
    {
        final int LogFileMaxLength = 8 * 1024 * 1024; // 8 MB

        Trace.writeLine("Program::EnableTraceLogging");

        if (operationArguments.WriteLog)
        {
            Trace.writeLine("   trace logging enabled");

            final AtomicReference<String> gitConfigPath = new AtomicReference<String>();
            if (Where.gitLocalConfig(gitConfigPath))
            {
                Trace.writeLine("   git local config found at " + gitConfigPath.get());

                final String dotGitPath = Path.getDirectoryName(gitConfigPath.get());
                final String logFilePath = Path.combine(dotGitPath, Path.changeExtension(ConfigPrefix, ".log"));
                final String logFileName = operationArguments.TargetUri.toString();

                final File logFileInfo = new File(logFilePath);
                if (logFileInfo.exists() && logFileInfo.length() > LogFileMaxLength)
                {
                    for (int i = 1; i < Integer.MAX_VALUE; i++)
                    {
                        final String moveName = String.format("%1$s%2$03d.log", ConfigPrefix, i);
                        final String movePath = Path.combine(dotGitPath, moveName);
                        final File moveFile = new File(movePath);

                        if (!moveFile.isFile())
                        {
                            logFileInfo.renameTo(moveFile);
                            break;
                        }
                    }
                }

                Trace.writeLine("   trace log destination is " + logFilePath);

                final PrintStream listener = new PrintStream(logFilePath);
                Trace.getListeners().add(listener);
                // write a small header to help with identifying new log entries
                listener.println(Environment.NewLine);
                listener.println(String.format("Log Start (%1$tFT%1$tT%1$tZ)", Calendar.getInstance()));
                listener.println(String.format("%1$s version %2$s", getTitle(), getVersion()));
            }
        }
    }

    private static void enableDebugTrace()
    {
        if (Debug.IsDebug)
        {
            // use the stderr stream for the trace as stdout is used in the cross-process communications protocol
            Trace.getListeners().add(System.err);
        }
    }

    static class ComponentFactory implements IComponentFactory
    {
        @Override public IAuthentication createAuthentication(final OperationArguments operationArguments, final ISecureStore secureStore)
        {
            return Program.createAuthentication(operationArguments, secureStore);
        }

        @Override public Configuration createConfiguration() throws IOException
        {
            return new Configuration();
        }

        @Override public ISecureStore createSecureStore(final OperationArguments operationArguments)
        {
            Trace.writeLine("Program::ComponentFactory::createSecureStore");
            ISecureStore secureStore = null;
            final File parentFolder = determineParentFolder();
            final File programFolder = new File(parentFolder, ProgramFolderName);
            final File insecureFile = new File(programFolder, "insecureStore.xml");

            final String osName = System.getProperty("os.name");
            if (Provider.isMac(osName))
            {
                Trace.writeLine("   Mac OS X detected");
                final KeychainSecurityCliStore keychainSecurityCliStore = new KeychainSecurityCliStore();
                boolean ableToUseKeychain = keychainSecurityCliStore.isKeychainAvailable();
                boolean canFallbackToInsecureStore = operationArguments.CanFallbackToInsecureStore;
                if (ableToUseKeychain)
                {
                    Trace.writeLine("     Keychain is available");
                    secureStore = keychainSecurityCliStore;
                    if (insecureFile.isFile())
                    {
                        Trace.writeLine("       InsecureStore file found");
                        if (!canFallbackToInsecureStore /* in other words, we won't use it again */)
                        {
                            Trace.writeLine("         No fallback requested, migrating...");
                            final InsecureStore insecureStore = new InsecureStore(insecureFile);
                            insecureStore.migrateAndDisable(secureStore);
                            Trace.writeLine("         Migrated and disabled.");
                        }
                        else
                        {
                            Trace.writeLine("         Fallback requested, skipping migration.");
                        }
                    }
                }
                else
                {
                    Trace.writeLine("     Keychain is NOT available");
                    if (!canFallbackToInsecureStore)
                    {
                        throw new SecurityException("The Keychain is not available. If you would like to use the InsecureStore instead, run `git config credential.canfallbacktoinsecurestore true`");
                    }
                }
            }

            if (secureStore == null)
            {
                Trace.writeLine("   Using the InsecureStore");
                //noinspection ResultOfMethodCallIgnored
                programFolder.mkdirs();
                secureStore = new InsecureStore(insecureFile);
            }
            return secureStore;
        }
    }
}
