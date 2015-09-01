package com.microsoft.alm.java.git_credential_helper.helpers;

public class Debug
{
    public static final boolean IsDebug;
    static
    {
        final String debug = System.getProperty("debug");
        IsDebug = (debug != null && !("0".equals(debug) || "false".equalsIgnoreCase(debug)));
    }
}
