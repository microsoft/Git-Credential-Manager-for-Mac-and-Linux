package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.ISecureStore;
import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

import java.net.URI;

public abstract class BaseSecureStore
{
    public static final char[] IllegalCharacters = { ':', ';', '\\', '?', '@', '=', '&', '%', '$' };

    private final ISecureStore delegate;

    protected BaseSecureStore(final ISecureStore delegate)
    {
        this.delegate = delegate;
    }

    protected void delete(final String targetName)
    {
        delegate.delete(targetName);
    }

    protected abstract String getTargetName(final URI targetUri);

    protected Credential readCredentials(final String targetName)
    {
        return delegate.readCredentials(targetName);
    }

    protected Token readToken(final String targetName)
    {
        return delegate.readToken(targetName);
    }

    protected void writeCredential(final String targetName, final Credential credentials)
    {
        delegate.writeCredential(targetName,  credentials);
    }

    protected void writeToken(final String targetName, final Token token)
    {
        delegate.writeToken(targetName, token);
    }

    static void validateTargetUri(final URI targetUri)
    {
        throw new NotImplementedException();
    }
}
