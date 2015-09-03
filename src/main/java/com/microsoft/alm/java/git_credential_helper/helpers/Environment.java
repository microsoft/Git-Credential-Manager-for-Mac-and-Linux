package com.microsoft.alm.java.git_credential_helper.helpers;

import java.io.File;

/**
 * Equivalent to the .NET class with the same name.
 */
public class Environment
{
    public static String getCurrentDirectory()
    {
        // http://stackoverflow.com/a/16802976/
        final File currentDir = new File("");
        final String result = currentDir.getAbsolutePath();
        return result;
    }
}
