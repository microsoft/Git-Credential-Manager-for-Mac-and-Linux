package com.microsoft.alm.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.Debug;
import com.microsoft.alm.java.git_credential_helper.helpers.ObjectExtensions;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;
import com.microsoft.alm.java.git_credential_helper.helpers.Trace;

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

    public SecretCache(final String namespace) { this(namespace,  null); }

    public SecretCache(final String namespace, final Secret.IUriNameConversion getTargetName)
    {
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(namespace), "The namespace parameter is null or invalid");

        _namespace = namespace;
        _getTargetName = ObjectExtensions.coalesce(getTargetName, Secret.DefaultUriNameConversion);
    }

    private final String _namespace;
    private final Secret.IUriNameConversion _getTargetName;

    /**
     * Deletes a credential from the cache.
     *
     * @param targetUri The URI of the target for which credentials are being deleted
     */
    public void deleteCredentials(final URI targetUri)
    {
        BaseSecureStore.validateTargetUri(targetUri);

        Trace.writeLine("SecretCache::deleteCredentials");

        final String targetName = this.getTargetName(targetUri);

        synchronized (_cache)
        {
            if (_cache.containsKey(targetName) && _cache.get(targetName) instanceof Credential)
            {
                _cache.remove(targetName);
            }
        }
    }

    /**
     * Deletes a token from the cache.
     *
     * @param targetUri The key which to find and delete the token with.
     */
    public void deleteToken(final URI targetUri)
    {
        BaseSecureStore.validateTargetUri(targetUri);

        Trace.writeLine("SecretCache::deleteToken");

        final String targetName = this.getTargetName(targetUri);

        synchronized (_cache)
        {
            if (_cache.containsKey(targetName) && _cache.get(targetName) instanceof Token)
            {
                _cache.remove(targetName);
            }
        }
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
        BaseSecureStore.validateTargetUri(targetUri);

        Trace.writeLine("SecretCache::readCredentials");

        final String targetName = this.getTargetName(targetUri);

        synchronized (_cache)
        {
            if (_cache.containsKey(targetName) && _cache.get(targetName) instanceof Credential)
            {
                credentials.set((Credential) _cache.get(targetName));
            }
            else
            {
                credentials.set(null);
            }
        }

        return credentials.get() != null;
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
        BaseSecureStore.validateTargetUri(targetUri);

        Trace.writeLine("SecretCache::readToken");

        final String targetName = this.getTargetName(targetUri);

        synchronized (_cache)
        {
            if (_cache.containsKey(targetName) && _cache.get(targetName) instanceof Token)
            {
                token.set((Token) _cache.get(targetName));
            }
            else
            {
                token.set(null);
            }
        }

        return token.get() != null;
    }

    /**
     * Writes credentials for a target URI to the credential store
     *
     * @param targetUri   The URI of the target for which credentials are being stored
     * @param credentials The credentials to be stored
     */
    public void writeCredentials(final URI targetUri, final Credential credentials)
    {
        BaseSecureStore.validateTargetUri(targetUri);
        Credential.validate(credentials);

        Trace.writeLine("SecretCache::writeCredentials");

        final String targetName = this.getTargetName(targetUri);

        synchronized (_cache)
        {
            _cache.put(targetName, credentials);
        }
    }

    /**
     * Writes a token to the cache.
     *
     * @param targetUri The key which to index the token by.
     * @param token     The token to write to the cache.
     */
    public void writeToken(final URI targetUri, final Token token)
    {
        BaseSecureStore.validateTargetUri(targetUri);
        Token.validate(token);

        Trace.writeLine("SecretCache::writeToken");

        final String targetName = this.getTargetName(targetUri);

        synchronized (_cache)
        {
            _cache.put(targetName, token);
        }
    }

    /**
     * Formats a TargetName string based on the TargetUri based on the format started by git-credential-winstore
     *
     * @param targetUri uri of the target
     * @return Properly formatted TargetName string
     */
    private String getTargetName(final URI targetUri)
    {
        Debug.Assert(targetUri != null, "The targetUri parameter is null");

        Trace.writeLine("SecretCache::_getTargetName");

        return _getTargetName.convert(targetUri, _namespace);
    }
}
