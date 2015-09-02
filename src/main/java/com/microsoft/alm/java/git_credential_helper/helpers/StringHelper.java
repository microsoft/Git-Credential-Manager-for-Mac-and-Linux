package com.microsoft.alm.java.git_credential_helper.helpers;

public class StringHelper
{
    public static final String Empty = "";

    public static boolean isNullOrWhiteSpace(final String s)
    {
        return null == s || (s.trim().length() == 0);
    }
}
