package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

final class Global
{
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
