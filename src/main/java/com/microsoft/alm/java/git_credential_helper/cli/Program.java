package com.microsoft.alm.java.git_credential_helper.cli;

import com.microsoft.alm.java.git_credential_helper.authentication.BaseVsoAuthentication;
import com.microsoft.alm.java.git_credential_helper.authentication.BasicAuthentication;
import com.microsoft.alm.java.git_credential_helper.authentication.Configuration;
import com.microsoft.alm.java.git_credential_helper.authentication.Credential;
import com.microsoft.alm.java.git_credential_helper.authentication.IAuthentication;
import com.microsoft.alm.java.git_credential_helper.authentication.ISecureStore;
import com.microsoft.alm.java.git_credential_helper.authentication.SecretStore;
import com.microsoft.alm.java.git_credential_helper.authentication.VsoAadAuthentication;
import com.microsoft.alm.java.git_credential_helper.authentication.VsoMsaAuthentication;
import com.microsoft.alm.java.git_credential_helper.authentication.VsoTokenScope;
import com.microsoft.alm.java.git_credential_helper.authentication.Where;
import com.microsoft.alm.java.git_credential_helper.helpers.Debug;
import com.microsoft.alm.java.git_credential_helper.helpers.Environment;
import com.microsoft.alm.java.git_credential_helper.helpers.Guid;
import com.microsoft.alm.java.git_credential_helper.helpers.InsecureStore;
import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;
import com.microsoft.alm.java.git_credential_helper.helpers.Path;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;
import com.microsoft.alm.java.git_credential_helper.helpers.Trace;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

public class Program
{
    private static final String ConfigPrefix = "credential";
    private static final String SecretsNamespace = "git";
    private static final VsoTokenScope VsoCredentialScope = VsoTokenScope.CodeWrite;

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
            final Program program = new Program(System.in, System.out, new IComponentFactory()
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
                    // TODO: detect the operating system/capabilities and create the appropriate instance
                    return new InsecureStore();
                }
            });

            program.innerMain(args);
        }
        catch (final Exception exception)
        {
            Trace.writeLine("Fatal: " + exception.toString());
            System.err.println("Fatal: " + exception.getClass().getName() + " encountered.");
            logEvent(exception.getMessage(), "EventLogEntryType.Error");
        }

        Trace.flush();
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
        throw new NotImplementedException();
    }

    private final Callable<Void> Get = new Callable<Void>()
    {
        @Override public Void call() throws IOException, URISyntaxException, ExecutionException, InterruptedException
        {
            get();
            return null;
        }
    };
    private void get() throws IOException, URISyntaxException, ExecutionException, InterruptedException
    {
        final AtomicReference<OperationArguments> operationArgumentsRef = new AtomicReference<OperationArguments>();
        final AtomicReference<IAuthentication> authenticationRef = new AtomicReference<IAuthentication>();
        initialize("get", operationArgumentsRef, authenticationRef);
        final String result = get(operationArgumentsRef.get(), authenticationRef.get());
        standardOut.print(result);
    }
    public static String get(final OperationArguments operationArguments, final IAuthentication authentication) throws ExecutionException, InterruptedException
    {
        final String AadMsaAuthFailureMessage = "Logon failed, use ctrl+c to cancel basic credential prompt.";
        final String GitHubAuthFailureMessage = "Logon failed, use ctrl+c to cancel basic credential prompt.";

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
                throw new NotImplementedException();

            case MicrosoftAccount:
                throw new NotImplementedException();

            case GitHub:
                throw new NotImplementedException();

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
        throw new NotImplementedException();
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
            IOUtils.closeQuietly(reader);
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

    private static IAuthentication createAuthentication(final OperationArguments operationArguments, final ISecureStore secureStore)
    {
        Debug.Assert(operationArguments != null, "The operationArguments is null");

        Trace.writeLine("Program::createAuthentication");

        final SecretStore secrets = new SecretStore(secureStore, SecretsNamespace);
        final AtomicReference<IAuthentication> authorityRef = new AtomicReference<IAuthentication>();

        if (operationArguments.Authority == AuthorityType.Auto)
        {
            Trace.writeLine("   detecting authority type");

            // detect the authority
            if (BaseVsoAuthentication.getAuthentication(operationArguments.TargetUri,
                    VsoCredentialScope,
                    secrets,
                    null,
                    authorityRef)
                    /* TODO: add GitHub support
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
                /* TODO: add GitHub support
                else if (authorityRef instanceof GithubAuthentication)
                {
                    operationArguments.Authority = AuthorityType.GitHub;
                }
                */
            }

            operationArguments.Authority = AuthorityType.Basic;
        }

        switch (operationArguments.Authority)
        {
            case AzureDirectory:
                Trace.writeLine("   authority is Azure Directory");

                UUID tenantId = Guid.Empty;
                // return the allocated authority or a generic AAD backed VSO authentication object
                return authorityRef.get() != null ? authorityRef.get() : new VsoAadAuthentication(Guid.Empty, VsoCredentialScope, secrets, null);

            case Basic:
            default:
                Trace.writeLine("   authority is basic");

                // return a generic username + password authentication object
                return authorityRef.get() != null ? authorityRef.get() : new BasicAuthentication(secrets);

            /* TODO: add GitHub support
            case GitHub:
                Trace.writeLine("    authority it GitHub");

                // return a GitHub authentication object
                return new GithubAuthentication(GithubCredentialScope, secrets);

            */
            case MicrosoftAccount:
                Trace.writeLine("   authority is Microsoft Live");

                // return the allocated authority or a generic MSA backed VSO authentication object
                return authorityRef.get() != null ? authorityRef.get() : new VsoMsaAuthentication(VsoCredentialScope, secrets, null);
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
}
