// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Debug;
import com.microsoft.alm.helpers.ObjectExtensions;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.helpers.Trace;
import com.microsoft.alm.secret.Credential;
import com.microsoft.alm.secret.Secret;
import com.microsoft.alm.secret.Token;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Interface to secure secrets storage which indexes values by target and utilizes the
 * operating system keychain / secrets vault.
 */
public final class SecretStore extends BaseSecureStore implements ICredentialStore, ITokenStore
{
    public SecretStore(final ISecureStore backingStore, final String namespace) { this(backingStore, namespace, null, null, null); }

    /**
     * Creates a new {@link SecretStore} backed by the specified keychain /
     * secrets vault.
     *
     * @param backingStore    The {@link ISecureStore} implementation to use for actually storing secrets.
     * @param namespace       The namespace of the secrets written and read by this store.
     * @param credentialCache (optional) Write-through, read-first cache. Default cache is used if a custom cache is
     *                        not provided.
     * @param tokenCache      (optional) Write-through, read-first cache. Default cache is used if a custom cache is
     *                        not provided.
     * @param getTargetName   The {@link Secret.IUriNameConversion} implementation to use for converting URIs to names.
     */
    public SecretStore(final ISecureStore backingStore, final String namespace, final ICredentialStore credentialCache, final ITokenStore tokenCache, final Secret.IUriNameConversion getTargetName)
    {
        super(backingStore);
        if (StringHelper.isNullOrWhiteSpace(namespace) || StringHelper.indexOfAny(namespace, IllegalCharacters) != -1)
            throw new IllegalArgumentException("The `namespace` parameter is null or invalid.");

        _getTargetName = ObjectExtensions.coalesce(getTargetName, Secret.DefaultUriNameConversion);

        _namespace = namespace;
        _credentialCache = credentialCache != null ? credentialCache : new SecretCache(namespace, _getTargetName);
        _tokenCache = tokenCache != null ? tokenCache : new SecretCache(namespace, _getTargetName);
    }

    private String _namespace;
    private ICredentialStore _credentialCache;
    private ITokenStore _tokenCache;

    private final Secret.IUriNameConversion _getTargetName;

    /**
     * Deletes credentials for target URI from the credential store
     *
     * @param targetUri The URI of the target for which credentials are being deleted
     */
    @Override public void deleteCredentials(final URI targetUri)
    {
        validateTargetUri(targetUri);

        Trace.writeLine("SecretStore::deleteCredentials");

        final String targetName = this.getTargetName(targetUri);

        this.delete(targetName);

        _credentialCache.deleteCredentials(targetUri);
    }

    /**
     * Deletes the {@link Token} for target URI from the token store
     *
     * @param targetUri The URI of the target for which the token is being deleted
     */
    @Override public void deleteToken(final URI targetUri)
    {
        validateTargetUri(targetUri);

        Trace.writeLine("SecretStore::deleteToken");

        final String targetName = this.getTargetName(targetUri);

        this.delete(targetName);
        _tokenCache.deleteToken(targetUri);
    }

    /**
     * Reads credentials for a target URI from the credential store
     *
     * @param targetUri   The URI of the target for which credentials are being read
     * @param credentials The credentials from the store; null if failure
     * @return            True if success; false if failure
     */
    @Override public boolean readCredentials(final URI targetUri, final AtomicReference<Credential> credentials)
    {
        validateTargetUri(targetUri);

        final String targetName = this.getTargetName(targetUri);

        Trace.writeLine("SecretStore::readCredentials");

        if (!_credentialCache.readCredentials(targetUri, credentials))
        {
            credentials.set(this.readCredentials(targetName));
        }

        return credentials.get() != null;
    }

    /**
     * Reads a {@link Token} for a target URI from the token store.
     *
     * @param targetUri The URI of the target for which a token is being read
     * @param token     The {@link Token} from the store; null if failure
     * @return True if success; false if failure
     */
    @Override public boolean readToken(final URI targetUri, final AtomicReference<Token> token)
    {
        validateTargetUri(targetUri);

        Trace.writeLine("SecretStore::readToken");

        token.set(null);

        if (!_tokenCache.readToken(targetUri, token))
        {
            final String targetName = this.getTargetName(targetUri);
            token.set(readToken(targetName));
        }

        return token.get() != null;
    }

    /**
     * Writes credentials for a target URI to the credential store
     *
     * @param targetUri   The URI of the target for which credentials are being stored
     * @param credentials The credentials to be stored
     */
    @Override public void writeCredentials(final URI targetUri, final Credential credentials)
    {
        validateTargetUri(targetUri);
        Credential.validate(credentials);

        Trace.writeLine("SecretStore::writeCredentials");

        final String targetName = this.getTargetName(targetUri);

        this.writeCredential(targetName, credentials);

        _credentialCache.writeCredentials(targetUri, credentials);
    }

    /**
     * Writes a {@link Token} for a target URI to the token store
     *
     * @param targetUri The URI of the target for which a token is being stored
     * @param token     The {@link Token} to be stored
     */
    @Override public void writeToken(final URI targetUri, final Token token)
    {
        validateTargetUri(targetUri);
        Token.validate(token);

        Trace.writeLine("SecretStore::writeToken");

        final String targetName = this.getTargetName(targetUri);

        _tokenCache.writeToken(targetUri, token);

        this.writeToken(targetName, token);
    }

    @Override protected String getTargetName(final URI targetUri)
    {

        Debug.Assert(targetUri != null, "The targetUri parameter is null");

        Trace.writeLine("SecretStore::getTargetName");

        return _getTargetName.convert(targetUri, _namespace);
    }
}
