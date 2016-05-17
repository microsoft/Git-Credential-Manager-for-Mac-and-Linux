// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Trace;
import com.microsoft.alm.secret.Credential;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public class BasicAuthentication extends BaseAuthentication implements IAuthentication
{
    /**
     * Creates a new {@link BasicAuthentication} object with an underlying credential store.
     *
     * @param credentialStore
     *        The {@link ICredentialStore} to delegate to.
     */
    public BasicAuthentication(final ICredentialStore credentialStore)
    {
        if (credentialStore == null)
            throw new IllegalArgumentException("The `credentialStore` parameter is null or invalid.");

        this.CredentialStore = credentialStore;
    }

    ICredentialStore CredentialStore;

    /**
     * Deletes a {@link Credential} from the storage used by the authentication object.
     *
     * @param targetUri
     *        The uniform resource indicator used to uniquely identify the credentials.
     */
    @Override public void deleteCredentials(final URI targetUri)
    {
        BaseSecureStore.validateTargetUri(targetUri);

        Trace.writeLine("BasicAuthentication::deleteCredentials");

        this.CredentialStore.deleteCredentials(targetUri);
    }
    /**
     * Gets a {@link Credential} from the storage used by the authentication object.
     *
     * @param targetUri
     *        The uniform resource indicator used to uniquely identify the credentials.
     *
     * @param credentials
     *        If successful a {@link Credential} object from the authentication object,
     *        authority or storage; otherwise null.
     *
     * @return true if successful; otherwise false.
     */
    @Override public boolean getCredentials(final URI targetUri, final AtomicReference<Credential> credentials)
    {
        BaseSecureStore.validateTargetUri(targetUri);

        Trace.writeLine("BasicAuthentication::getCredentials");

        this.CredentialStore.readCredentials(targetUri, credentials);

        return credentials.get() != null;
    }
    /**
     * Sets a {@link Credential} in the storage used by the authentication object.
     *
     * @param targetUri
     *        The uniform resource indicator used to uniquely identify the credentials.
     *
     * @param credentials The value to be stored
     *
     * @return true if successful; otherwise false.
     */
    @Override public boolean setCredentials(final URI targetUri, final Credential credentials)
    {
        BaseSecureStore.validateTargetUri(targetUri);
        Credential.validate(credentials);

        Trace.writeLine("BasicAuthentication::setCredentials");

        this.CredentialStore.writeCredentials(targetUri, credentials);
        return true;
    }
}
