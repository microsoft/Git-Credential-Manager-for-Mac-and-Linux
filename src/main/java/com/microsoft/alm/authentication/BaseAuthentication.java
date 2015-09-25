package com.microsoft.alm.authentication;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base authentication mechanisms for setting, retrieving, and deleting stored credentials.
 */
public abstract class BaseAuthentication implements IAuthentication
{
    /**
     * Deletes a {@link Credential} from the storage used by the authentication object.
     *
     * @param targetUri
     *        The uniform resource indicator used to uniquely identify the credentials.
     */
    public abstract void deleteCredentials(final URI targetUri);

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
    public abstract boolean getCredentials(final URI targetUri, final AtomicReference<Credential> credentials);

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
    public abstract boolean setCredentials(final URI targetUri, final Credential credentials);
}
