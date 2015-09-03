package com.microsoft.alm.java.git_credential_helper.helpers;

import java.io.File;

/**
 * Equivalent to System.IO.Path
 */
public class Path
{
    public static String combine(final String path1, final String path2)
    {
        final File file = new File(path1, path2);
        return file.getPath();
    }

    public static String getDirectoryName(final String path)
    {
        final File file = new File(path);
        return file.getParent();
    }

    public static boolean isAbsolute(final String path)
    {
        final File file = new File(path);
        return file.isAbsolute();
    }
}
