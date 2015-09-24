package com.microsoft.alm.java.git_credential_helper.helpers;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Equivalent to System.Diagnostics.Trace
 */
// TODO: Wire this up to some logging framework?
public class Trace
{
    private static final List<PrintStream> listeners = new ArrayList<PrintStream>();

    public static void flush()
    {
        for (final PrintStream listener : listeners)
        {
            listener.flush();
        }
    }

    public static List<PrintStream> getListeners()
    {
        return listeners;
    }

    public static void writeLine(final String message)
    {
        for (final PrintStream listener : listeners)
        {
            listener.println(message);
        }
    }
}
