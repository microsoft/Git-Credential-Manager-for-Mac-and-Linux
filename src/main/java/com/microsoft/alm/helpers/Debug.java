package com.microsoft.alm.helpers;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Debug
{
    private static final String ASSERTION_FAILED_TITLE = "Assertion Failed: Yes=Quit, No=Continue";
    public static final boolean IsDebug;
    static
    {
        final String debug = System.getProperty("debug");
        IsDebug = (debug != null && !("0".equals(debug) || "false".equalsIgnoreCase(debug)));
    }

    public static void Assert(final boolean condition, final String message)
    {
        if (IsDebug)
        {
            if (!condition)
            {
                // http://stackoverflow.com/a/1069150/
                final Throwable throwable = new Throwable();
                final StackTraceElement[] stackTraceElements = throwable.getStackTrace();
                final StringBuilder sb = new StringBuilder();
                sb.append(message);
                sb.append("\n");
                sb.append("\n");
                boolean first = true;
                for (final StackTraceElement stackTraceElement : stackTraceElements)
                {
                    if (first)
                    {
                       first = false;
                    }
                    else
                    {
                        sb.append(stackTraceElement.toString());
                        sb.append("\n");
                    }
                }
                final String fullMessage = sb.toString();

                // http://stackoverflow.com/a/543012/
                final JFrame frame = new JFrame(ASSERTION_FAILED_TITLE);
                final int result;
                try
                {
                    frame.setUndecorated(true);
                    frame.setVisible(true);
                    frame.setLocationRelativeTo(null);
                    result = JOptionPane.showConfirmDialog(
                            frame,
                            fullMessage,
                            ASSERTION_FAILED_TITLE,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE,
                            null
                    );
                }
                finally
                {
                    frame.dispose();
                }
                if (JOptionPane.YES_OPTION == result)
                {
                    System.exit(1);
                }
            }

        }
    }
}
