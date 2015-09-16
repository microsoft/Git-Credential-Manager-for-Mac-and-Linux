package com.microsoft.alm.java.git_credential_helper.cli;

import com.microsoft.alm.java.git_credential_helper.authentication.BaseAuthentication;
import com.microsoft.alm.java.git_credential_helper.authentication.Configuration;
import com.microsoft.alm.java.git_credential_helper.authentication.IAuthentication;
import com.microsoft.alm.java.git_credential_helper.helpers.Debug;
import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;
import com.microsoft.alm.java.git_credential_helper.helpers.Trace;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

public class Program
{
    private static final String ConfigPrefix = "credential";

    private final InputStream standardIn;
    private final PrintStream standardOut;

    public static void main(final String[] args)
    {
        try
        {
            enableDebugTrace();
            final Program program = new Program(System.in, System.out);

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

    public Program(final InputStream standardIn, final PrintStream standardOut)
    {
        this.standardIn = standardIn;
        this.standardOut = standardOut;
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
        @Override public Void call()
        {
            erase();
            return null;
        }
    };
    private void erase()
    {
        throw new NotImplementedException();
    }

    private final Callable<Void> Get = new Callable<Void>()
    {
        @Override public Void call()
        {
            get();
            return null;
        }
    };
    private void get()
    {
        throw new NotImplementedException();
    }

    private final Callable<Void> Store = new Callable<Void>()
    {
        @Override public Void call()
        {
            store();
            return null;
        }
    };
    private void store()
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
        throw new NotImplementedException();
    }

    private static IAuthentication createAuthentication(final OperationArguments operationArguments)
    {
        throw new NotImplementedException();
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

    private static void enableDebugTrace()
    {
        if (Debug.IsDebug)
        {
            // use the stderr stream for the trace as stdout is used in the cross-process communications protocol
            Trace.getListeners().add(System.err);
        }
    }
}
