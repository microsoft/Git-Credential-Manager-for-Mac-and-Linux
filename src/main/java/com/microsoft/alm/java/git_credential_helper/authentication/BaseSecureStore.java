package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

import java.net.URI;

public abstract class BaseSecureStore
{
    public static final char[] IllegalCharacters = { ':', ';', '\\', '?', '@', '=', '&', '%', '$' };

    protected void delete(final String targetName)
    {
        throw new NotImplementedException();
    }

    protected abstract String getTargetName(final URI targetUri);

    protected Credential readCredentials(final String targetName)
    {
        throw new NotImplementedException();
    }

    protected Token readToken(final String targetName)
    {
        throw new NotImplementedException();
    }

    protected void writeCredential(final String targetName, final Credential credentials)
    {
        throw new NotImplementedException();
    }

    protected void writeToken(final String targetName, final Token token)
    {
        throw new NotImplementedException();
    }

    static void validateTargetUri(final URI targetUri)
    {
        throw new NotImplementedException();
    }
}
