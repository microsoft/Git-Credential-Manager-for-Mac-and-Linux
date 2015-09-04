package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Interface to secure secrets storage which indexes values by target and utilizes the
 * operating system keychain / secrets vault.
 */
public final class SecretStore extends BaseSecureStore implements ICredentialStore, ITokenStore
{
    /**
     * Creates a new {@SecretStore} backed by the operating system keychain /
     * secrets vault.
     *
     * @param namespace       The namespace of the secrets written and read by this store.
     * @param credentialCache (optional) Write-through, read-first cache. Default cache is used if a custom cache is
     *                        not provided.
     * @param tokenCache      (optional) Write-through, read-first cache. Default cache is used if a custom cache is
     *                        not provided.
     */
    public SecretStore(final String namespace, final ICredentialStore credentialCache, final ITokenStore tokenCache)
    {
        throw new NotImplementedException();
    }

    private String _namespace;
    private ICredentialStore _credentialCache;
    private ITokenStore _tokenCache;

    /**
     * Deletes credentials for target URI from the credential store
     *
     * @param targetUri The URI of the target for which credentials are being deleted
     */
    @Override public void deleteCredentials(final URI targetUri)
    {
        throw new NotImplementedException();
    }

    /**
     * Deletes the {@link Token} for target URI from the token store
     *
     * @param targetUri The URI of the target for which the token is being deleted
     */
    @Override public void deleteToken(final URI targetUri)
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
    @Override public boolean readCredentials(final URI targetUri, final AtomicReference<Credential> credentials)
    {
        throw new NotImplementedException();
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
        throw new NotImplementedException();
    }

    /**
     * Writes credentials for a target URI to the credential store
     *
     * @param targetUri   The URI of the target for which credentials are being stored
     * @param credentials The credentials to be stored
     */
    @Override public void writeCredentials(final URI targetUri, final Credential credentials)
    {
        throw new NotImplementedException();
    }

    /**
     * Writes a {@link Token} for a target URI to the token store
     *
     * @param targetUri The URI of the target for which a token is being stored
     * @param token     The {@link Token} to be stored
     */
    @Override public void writeToken(final URI targetUri, final Token token)
    {
        throw new NotImplementedException();
    }

    @Override protected String getTargetName(final URI targetUri)
    {
        throw new NotImplementedException();
    }
}
