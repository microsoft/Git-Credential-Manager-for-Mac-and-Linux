package com.microsoft.alm.java.git_credential_helper.helpers;

import java.net.URI;
import java.net.URISyntaxException;

public class UriHelper
{
    public static boolean isWellFormedUriString(final String uriString)
    {
        try
        {
            new URI(uriString);
            return true;
        }
        catch (final URISyntaxException ignored)
        {
            return false;
        }
    }
}
