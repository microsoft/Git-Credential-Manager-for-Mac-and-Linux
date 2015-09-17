package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

final class Global
{
    public static final int PasswordMaxLength = 2047;
    public static final int UsernameMaxLength = 511;

    private static String userAgent = null;

    /**
     * Creates the correct user-agent string for HTTP calls.
     *
     * @return The `user-agent` string for "git-tools".
     */
    public static String getUserAgent()
    {
        throw new NotImplementedException();
    }
}
