package com.microsoft.alm.java.git_credential_helper.authentication;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public interface IAuthentication
{
    void deleteCredentials(final URI targetUri);
    boolean getCredentials(final URI targetUri, final AtomicReference<Credential> credentials);
    boolean setCredentials(final URI targetUri, final Credential credentials);
}
