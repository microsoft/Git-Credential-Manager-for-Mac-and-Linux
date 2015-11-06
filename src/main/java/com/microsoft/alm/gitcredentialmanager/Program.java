// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

import com.microsoft.alm.authentication.BaseVsoAuthentication;
import com.microsoft.alm.authentication.BasicAuthentication;
import com.microsoft.alm.authentication.Configuration;
import com.microsoft.alm.authentication.Credential;
import com.microsoft.alm.authentication.IAuthentication;
import com.microsoft.alm.authentication.ISecureStore;
import com.microsoft.alm.authentication.ITokenStore;
import com.microsoft.alm.authentication.IVsoAadAuthentication;
import com.microsoft.alm.authentication.IVsoMsaAuthentication;
import com.microsoft.alm.authentication.SecretStore;
import com.microsoft.alm.authentication.VsoAadAuthentication;
import com.microsoft.alm.authentication.VsoMsaAuthentication;
import com.microsoft.alm.authentication.VsoTokenScope;
import com.microsoft.alm.authentication.Where;
import com.microsoft.alm.helpers.Debug;
import com.microsoft.alm.helpers.Environment;
import com.microsoft.alm.helpers.Guid;
import com.microsoft.alm.helpers.IOHelper;
import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.helpers.Path;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.helpers.Trace;
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
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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

    private final InputStream standardIn;
    private final PrintStream standardOut;
    private final IComponentFactory componentFactory;

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
        catch (final Exception exception)
        {
            if (Debug.IsDebug)
            {
                System.err.println("Fatal error encountered.  Details:");
                exception.printStackTrace(System.err);
            }
            else
            {
                System.err.println("Fatal: " + exception.getClass().getName() + " encountered.  Details:");
                System.err.println(exception.getMessage());
            }
            logEvent(exception.getMessage(), "EventLogEntryType.Error");
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
        standardOut.println("   authority      Defines the type of authentication to be used.");
        standardOut.println("                  Supports Auto, Basic, AAD, MSA, and Integrated.");
        standardOut.println("                  Default is Auto.");
        standardOut.println();
        standardOut.println("      `git config --global credential.microsoft.visualstudio.com.authority AAD`");
        standardOut.println();
        standardOut.println("   interactive    Specifies if user can be prompted for credentials or not.");
        standardOut.println("                  Supports Auto, Always, or Never. Defaults to Auto.");
        standardOut.println("                  Only used by AAD and MSA authority.");
        standardOut.println();
        standardOut.println("      `git config --global credential.microsoft.visualstudio.com.interactive never`");
        standardOut.println();
        standardOut.println("   validate       Causes validation of credentials before supplying them");
        standardOut.println("                  to Git. Invalid credentials get a refresh attempt");
        standardOut.println("                  before failing. Incurs some minor overhead.");
        standardOut.println("                  Defaults to TRUE. Ignored by Basic authority.");
        standardOut.println();
        standardOut.println("      `git config --global credential.microsoft.visualstudio.com.validate false`");
        standardOut.println();
        standardOut.println("   writelog       Enables trace logging of all activities. Logs are written to");
        standardOut.println("                  the .git/ folder at the root of the repository.");
        standardOut.println("                  Defaults to FALSE.");
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
        final String result = get(operationArgumentsRef.get(), authenticationRef.get());
        standardOut.print(result);
    }
    public static String get(final OperationArguments operationArguments, final IAuthentication authentication)
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
                if (((operationArguments.Interactivity != Interactivity.Always
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
                            && aadAuth.interactiveLogon(operationArguments.TargetUri, true))
                            && aadAuth.getCredentials(operationArguments.TargetUri, credentials)
                            && (!operationArguments.ValidateCredentials
                                || aadAuth.validateCredentials(operationArguments.TargetUri, credentials.get()))))
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
                if (((operationArguments.Interactivity != Interactivity.Always
                        && msaAuth.getCredentials(operationArguments.TargetUri, credentials)
                        && (!operationArguments.ValidateCredentials
                            || msaAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                        || (operationArguments.Interactivity != Interactivity.Always
                            && msaAuth.refreshCredentials(operationArguments.TargetUri, true)
                            && msaAuth.getCredentials(operationArguments.TargetUri, credentials)
                            && (!operationArguments.ValidateCredentials
                                || msaAuth.validateCredentials(operationArguments.TargetUri, credentials.get())))
                        || (operationArguments.Interactivity != Interactivity.Never
                            && msaAuth.interactiveLogon(operationArguments.TargetUri, true))
                            && msaAuth.getCredentials(operationArguments.TargetUri, credentials)
                            && (!operationArguments.ValidateCredentials
                                || msaAuth.validateCredentials(operationArguments.TargetUri, credentials.get()))))
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
        store(operationArgumentsRef.get(), authenticationRef.get());
    }
    public static void store(final OperationArguments operationArguments, final IAuthentication authentication)
    {
        Debug.Assert(operationArguments.getUserName() != null, "The operationArguments.Username is null");

        final Credential credentials = new Credential(operationArguments.getUserName(), operationArguments.getPassword());
        authentication.setCredentials(operationArguments.TargetUri, credentials);
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
        missedRequirements.addAll(checkUserAgentProviderRequirements(providers));
        missedRequirements.addAll(checkGitRequirements());
        missedRequirements.addAll(checkOsRequirements(osName, osVersion));

        if (missedRequirements.isEmpty())
        {
            try
            {
                // TODO: 457304: Add option to configure for global or system
                // TODO: test with spaces in JAR path
                // TODO: uninstall ourselves first, possibly both from global and system, because
                // we don't want another version of ourselves answering credential requests, too!
                final URL resourceURL = Program.class.getResource("");
                final String pathToJar = determinePathToJar(resourceURL);
                final TestableProcess process = processFactory.create("git", "config", "--global", "credential.helper", "!java -Ddebug=false -jar " + pathToJar);
                final ProcessCoordinator coordinator = new ProcessCoordinator(process);
                final int exitCode = coordinator.waitFor();
                String message;
                switch (exitCode)
                {
                    case 0:
                        message = null;
                        break;
                    case 3:
                        // TODO: 457304: Be specific as to which config file
                        message = "The 'global' Git config file is invalid.";
                        break;
                    case 4:
                        // TODO: 457304: Be specific as to which config file
                        message = "Can not write to the 'global' Git config file.";
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
            catch (IOException e)
            {
                throw new Error(e);
            }
            catch (InterruptedException e)
            {
                throw new Error(e);
            }
        }
        else
        {
            standardOut.println("Installation failed due to the following requirement issues:");
            for (String msg : missedRequirements)
            {
                standardOut.println(msg);
            }
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
        final String packagePath = packageName.replace(".", "/");
        final String resourceSuffix = "!/" + packagePath + "/";
        String jarPath = resourcePath.replace(resourceSuffix, "");
        jarPath = jarPath.replace("file:", "");
        return jarPath;
    }

    /**
     * Asks all the supplied {@link Provider} implementations to check their requirements and
     * report only if all of them are missing something.
     *
     * @param providers a list of {@link Provider} implementations to interrogate
     * @return a list of requirements, per provider,
     *          if no single user agent provider had all its requirements satisfied
     */
    static List<String> checkUserAgentProviderRequirements(final List<Provider> providers)
    {
        final List<String> results = new ArrayList<String>();
        final LinkedHashMap<Provider, List<String>> requirementsByProvider = new LinkedHashMap<Provider, List<String>>();
        int numberOfProvidersWithSatisfiedRequirements = 0;
        for (final Provider provider : providers)
        {
            final List<String> requirements = provider.checkRequirements();
            if (requirements == null || requirements.isEmpty())
            {
                numberOfProvidersWithSatisfiedRequirements++;
            }
            else
            {
                requirementsByProvider.put(provider, requirements);
            }
        }
        if (numberOfProvidersWithSatisfiedRequirements == 0)
        {
            for (final Map.Entry<Provider, List<String>> entry : requirementsByProvider.entrySet())
            {
                final Provider provider = entry.getKey();
                final List<String> requirements = entry.getValue();
                results.add("The " + provider.getClassName() + " user agent provider has the following unmet requirements:");
                for (final String requirement : requirements)
                {
                    results.add(" - " + requirement);
                }
            }
        }
        return results;
    }

    /**
     * Checks if git version can be found and if it is the correct version
     *
     * @return if git requirements are met
     */
    private static List<String> checkGitRequirements()
    {
        Reader inputStream = null;
        BufferedReader bufferedReader = null;
        try
        {
            // finding git version via commandline
            final Process gitProcess = new ProcessBuilder("git", "--version").start();
            gitProcess.waitFor();
            inputStream = new InputStreamReader(gitProcess.getInputStream());
            bufferedReader = new BufferedReader(inputStream);
            final String gitResponse = bufferedReader.readLine();
            return isValidGitVersion(gitResponse);
        }
        catch (final IOException e)
        {
            throw new Error(e);
        }
        catch (final InterruptedException e)
        {
            throw new Error(e);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    inputStream.close();
                }
                if (bufferedReader != null)
                {
                    bufferedReader.close();
                }
            }
            catch (final IOException ignored)
            {
            }
        }
    }

    /**
     * Parses git version response for major and minor version and checks if it's 1.9 or above
     *
     * @param gitResponse the output from 'git --version'
     * @return if the git version meets the requirement
     */
    protected static List<String> isValidGitVersion(String gitResponse)
    {
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
     * @return if OS requirements are met
     */
    protected static List<String> checkOsRequirements(final String osName, final String osVersion)
    {
        if (Provider.isMac(osName))
        {
            Version version = Version.parseVersion(osVersion);
            List<String> badVersionMessage = Arrays.asList("The version of Mac OS X running is " + version.getMajor() + "." + version.getMinor() + "." + version.getPatch() +
                    " which does not meet the minimum version of 10.10.5 needed for installation. Please upgrade to Mac OS X 10.10.5 or above to proceed.");
            if (version.getMajor() > 10)
            {
                return Collections.emptyList();
            }
            else if (version.getMajor() < 10)
            {
                return badVersionMessage;
            }
            else if (version.getMinor() > 10)
            {
                return Collections.emptyList();
            }
            else if (version.getMinor() < 10)
            {
                return badVersionMessage;
            }
            else if (version.getPatch() >= 5)
            {
                return Collections.emptyList();
            }
            else
            {
                return badVersionMessage;
            }
        }
        else if (Provider.isLinux(osName))
        {
            // only needs to be a desktop env which is already checked within checkJavaRequirements()
            return Collections.emptyList();
        }
        else
        {
            return Arrays.asList("Git Credential Manager only runs on Mac OS X and Linux. The operating system detected is " + osName + " which is not supported");
        }
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

        final ISecureStore secureStore = componentFactory.createSecureStore();
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

        @Override public ISecureStore createSecureStore()
        {
            // TODO: 449516: detect the operating system/capabilities and create the appropriate instance
            final File parentFolder = determineParentFolder();
            final File programFolder = new File(parentFolder, ProgramFolderName);
            //noinspection ResultOfMethodCallIgnored
            programFolder.mkdirs();
            final File insecureFile = new File(programFolder, "insecureStore.xml");
            return new InsecureStore(insecureFile);
        }
    }
}
