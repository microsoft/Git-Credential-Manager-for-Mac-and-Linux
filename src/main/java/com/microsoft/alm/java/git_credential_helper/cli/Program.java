package com.microsoft.alm.java.git_credential_helper.cli;

import com.microsoft.alm.java.git_credential_helper.helpers.Debug;
import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;
import com.microsoft.alm.java.git_credential_helper.helpers.Trace;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

public class Program
{
    public static void main(final String[] args)
    {
        try
        {
            enableDebugTrace();
            
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
        catch (final Exception exception)
        {
            Trace.writeLine("Fatal: " + exception.toString());
            System.err.println("Fatal: " + exception.getClass().getName() + " encountered.");
            logEvent(exception.getMessage(), "EventLogEntryType.Error");
        }

        Trace.flush();
    }

    private static void printHelpMessage()
    {
        Trace.writeLine("Program::printHelpMessage");

        System.out.println("usage: git credential <command> [<args>]");
        System.out.println();
        System.out.println("   authority      Defines the type of authentication to be used.");
        System.out.println("                  Supports Auto, Basic, AAD, MSA, and Integrated.");
        System.out.println("                  Default is Auto.");
        System.out.println();
        System.out.println("      `git config --global credential.microsoft.visualstudio.com.authority AAD`");
        System.out.println();
        System.out.println("   interactive    Specifies if user can be prompted for credentials or not.");
        System.out.println("                  Supports Auto, Always, or Never. Defaults to Auto.");
        System.out.println("                  Only used by AAD and MSA authority.");
        System.out.println();
        System.out.println("      `git config --global credential.microsoft.visualstudio.com.interactive never`");
        System.out.println();
        System.out.println("   validate       Causes validation of credentials before supplying them");
        System.out.println("                  to Git. Invalid credentials get a refresh attempt");
        System.out.println("                  before failing. Incurs some minor overhead.");
        System.out.println("                  Defaults to TRUE. Ignored by Basic authority.");
        System.out.println();
        System.out.println("      `git config --global credential.microsoft.visualstudio.com.validate false`");
        System.out.println();
        System.out.println("   writelog       Enables trace logging of all activities. Logs are written to");
        System.out.println("                  the .git/ folder at the root of the repository.");
        System.out.println("                  Defaults to FALSE.");
        System.out.println();
        System.out.println("      `git config --global credential.writelog true`");
        System.out.println();
        System.out.println("Sample Configuration:");
        System.out.println("   [credential \"microsoft.visualstudio.com\"]");
        System.out.println("       authority = AAD");
        System.out.println("   [credential \"visualstudio.com\"]");
        System.out.println("       authority = MSA");
        System.out.println("   [credential]");
        System.out.println("       helper = manager");
    }

    private static final Callable<Void> Erase = new Callable<Void>()
    {
        @Override public Void call()
        {
            erase();
            return null;
        }
    };
    private static void erase()
    {
        throw new NotImplementedException();
    }

    private static final Callable<Void> Get = new Callable<Void>()
    {
        @Override public Void call()
        {
            get();
            return null;
        }
    };
    private static void get()
    {
        throw new NotImplementedException();
    }

    private static final Callable<Void> Store = new Callable<Void>()
    {
        @Override public Void call()
        {
            store();
            return null;
        }
    };
    private static void store()
    {
        throw new NotImplementedException();
    }

    private static final Callable<Void> PrintVersion = new Callable<Void>()
    {
        @Override public Void call()
        {
            printVersion();
            return null;
        }
    };
    private static void printVersion()
    {
        throw new NotImplementedException();
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
