package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

final class SecretCache implements ICredentialStore, ITokenStore
{
    static
    {
        _cache = new TreeMap<String, Secret>(String.CASE_INSENSITIVE_ORDER);
    }

    private static final Map<String, Secret> _cache;

    public SecretCache(final String namespace)
    {
        throw new NotImplementedException();
    }

    private final String _namespace;

    /**
     * Deletes a credential from the cache.
     *
     * @param targetUri The URI of the target for which credentials are being deleted
     */
    public void deleteCredentials(final URI targetUri)
    {
        throw new NotImplementedException();
    }

    /**
     * Deletes a token from the cache.
     *
     * @param targetUri The key which to find and delete the token with.
     */
    public void deleteToken(final URI targetUri)
    {
        throw new NotImplementedException();
    }

    /**
     * Reads credentials for a target URI from the credential store
     *
     * @param targetUri   The URI of the target for which credentials are being read
     * @param credentials The credentials from the store; null if failure
     * @return            True if success; false if failure
     */
    public boolean readCredentials(final URI targetUri, final AtomicReference<Credential> credentials)
    {
        throw new NotImplementedException();
    }

    /**
     * Gets a token from the cache.
     *
     * @param targetUri The key which to find the token.
     * @param token     The token if successful; otherwise null.
     * @return          True if successful; false otherwise.
     */
    public boolean readToken(final URI targetUri, final AtomicReference<Token> token)
    {
        throw new NotImplementedException();
    }

    /**
     * Writes credentials for a target URI to the credential store
     *
     * @param targetUri   The URI of the target for which credentials are being stored
     * @param credentials The credentials to be stored
     */
    public void writeCredentials(final URI targetUri, final Credential credentials)
    {
        throw new NotImplementedException();
    }

    /**
     * Writes a token to the cache.
     *
     * @param targetUri The key which to index the token by.
     * @param token     The token to write to the cache.
     */
    public void writeToken(final URI targetUri, final Token token)
    {
        throw new NotImplementedException();
    }

    private String getTargetName(final URI targetUri)
    {
        throw new NotImplementedException();
    }
}
