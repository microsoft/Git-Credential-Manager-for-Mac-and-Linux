package com.microsoft.alm.java.git_credential_helper.authentication;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public interface ICredentialStore
{
    void deleteCredentials(final URI targetUri);
    boolean readCredentials(final URI targetUri, final AtomicReference<Credential> credentials);
    void writeCredentials(final URI targetUri, final Credential credentials);
}
