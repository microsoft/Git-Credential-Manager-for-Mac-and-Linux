package com.microsoft.alm.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.Trace;

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
        Trace.writeLine("BaseSecureStore::delete");

        try
        {
            delegate.delete(targetName);
        }
        catch (final Throwable throwable)
        {
            Trace.writeLine(throwable.toString());
        }
    }

    protected abstract String getTargetName(final URI targetUri);

    protected Credential readCredentials(final String targetName)
    {
        Trace.writeLine("BaseSecureStore::readCredentials");

        return delegate.readCredentials(targetName);
    }

    protected Token readToken(final String targetName)
    {
        Trace.writeLine("BaseSecureStore::readToken");

        return delegate.readToken(targetName);
    }

    protected void writeCredential(final String targetName, final Credential credentials)
    {
        Trace.writeLine("BaseSecureStore::writeCredential");

        delegate.writeCredential(targetName,  credentials);
    }

    protected void writeToken(final String targetName, final Token token)
    {
        Trace.writeLine("BaseSecureStore::writeToken");

        delegate.writeToken(targetName, token);
    }

    static void validateTargetUri(final URI targetUri)
    {
        if (targetUri == null)
            throw new IllegalArgumentException("targetUri");
        if (!targetUri.isAbsolute())
            throw new IllegalArgumentException("The target URI must be an absolute URI");
    }
}
