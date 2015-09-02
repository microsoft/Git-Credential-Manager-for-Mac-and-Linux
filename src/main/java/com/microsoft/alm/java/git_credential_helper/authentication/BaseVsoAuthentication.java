package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base functionality for performing authentication operations against Visual Studio Online.
 */
public abstract class BaseVsoAuthentication extends BaseAuthentication
{
    public static final String DefaultResource = "499b84ac-1321-427f-aa17-267ca6975798";
    public static final String DefaultClientId = "872cd9fa-d31f-45e0-9eab-6e460a02d1f1";
    public static final String RedirectUrl = "urn:ietf:wg:oauth:2.0:oob";

    protected static final String AdalRefreshPrefx = "ada";

    private BaseVsoAuthentication(final VsoTokenScope tokenScope, final ICredentialStore personalAccessTokenStore)
    {
        throw new NotImplementedException();
    }
    /**
     * Invoked by a derived classes implementation. Allows custom back-end implementations to be used.
     *
     * @param tokenScope The desired scope of the acquired personal access token(s).
     * @param personalAccessTokenStore The secret store for acquired personal access token(s).
     * @param adaRefreshTokenStore The secret store for acquired Azure refresh token(s).
     */
    protected BaseVsoAuthentication(
            final VsoTokenScope tokenScope,
            final ICredentialStore personalAccessTokenStore,
            final ITokenStore adaRefreshTokenStore
    )
    {
        throw new NotImplementedException();
    }
    BaseVsoAuthentication(
            final ICredentialStore personalAccessTokenStore,
            final ITokenStore adaRefreshTokenStore,
            final ITokenStore vsoIdeTokenCache,
            final IVsoAuthority vsoAuthority)
    {
        throw new NotImplementedException();
    }

    /**
     * The application client identity by which access will be requested.
     */
    public final String ClientId;
    /**
     * The Azure resource for which access will be requested.
     */
    public final String Resource;
    /**
     * The desired scope of the authentication token to be requested.
     */
    public final VsoTokenScope TokenScope;

    // TODO: final TokenCache VsoAdalTokenCache;
    final ITokenStore VsoIdeTokenCache;

    ICredentialStore PersonalAccessTokenStore;
    ITokenStore AdaRefreshTokenStore;
    IVsoAuthority VsoAuthority;
    UUID TenantId;

    /**
     * Deletes a set of stored credentials by their target resource.
     *
     * @param targetUri The 'key' by which to identify credentials.
     */
    @Override public void deleteCredentials(final URI targetUri)
    {
        throw new NotImplementedException();
    }

    /**
     * Attempts to get a set of credentials from storage by their target resource.
     *
     * @param targetUri   The 'key' by which to identify credentials.
     * @param credentials Credentials associated with the URI if successful; null otherwise.
     * @return True if successful; false otherwise.
     */
    @Override public boolean getCredentials(final URI targetUri, final AtomicReference<Credential> credentials)
    {
        throw new NotImplementedException();
    }

    /**
     * Attempts to generate a new personal access token (credentials) via use of a stored
     * Azure refresh token, identified by the target resource.
     *
     * @param targetUri           The 'key' by which to identify the refresh token.
     * @param requireCompactToken Generates a compact token if true; generates a self
     *                            describing token if false.
     * @return True if successful; false otherwise.
     */
    public Future<Boolean> refreshCredentials(final URI targetUri, final boolean requireCompactToken)
    {
        throw new NotImplementedException();
    }

    /**
     * Validates that a set of credentials grants access to the target resource.
     *
     * @param targetUri   The target resource to validate against.
     * @param credentials The credentials to validate.
     * @return True if successful; false otherwise.
     */
    public Future<Boolean> validateCredentials(final URI targetUri, final Credential credentials)
    {
        throw new NotImplementedException();
    }

    /**
     *
     *
     * @param targetUri           The target resource for which to acquire the personal access
     *                            token for.
     * @param accessToken         Azure Directory access token with privileges to grant access
     *                            to the target resource.
     * @param requestCompactToken Generates a compact token if true;
     *                            generates a self describing token if false.
     * @return True if successful; false otherwise.
     */
    protected Future<Boolean> generatePersonalAccessToken(final URI targetUri, final Token accessToken, final boolean requestCompactToken)
    {
        throw new NotImplementedException();
    }

    /**
     * Stores an Azure Directory refresh token.
     *
     * @param targetUri    The 'key' by which to identify the token.
     * @param refreshToken The token to be stored.
     */
    protected void storeRefreshToken(final URI targetUri, final Token refreshToken)
    {
        throw new NotImplementedException();
    }

    /**
     * Detects the backing authority of the end-point.
     *
     * @param target   The resource which the authority protects.
     * @param tenantId The identity of the authority tenant; null otherwise.
     * @return True if the authority is Visual Studio Online; false otherwise.
     */
    public static boolean detectAuthority(final URI target, final AtomicReference<UUID> tenantId)
    {
        throw new NotImplementedException();
    }

    /**
     * Creates a new authentication broker based for the specified resource.
     *
     * @param targetUri                The resource for which authentication is being requested.
     * @param scope                    The scope of the access being requested.
     * @param personalAccessTokenStore Storage container for personal access token secrets.
     * @param adaRefreshTokenStore     Storage container for Azure access token secrets.
     * @param authentication           An implementation of {@link BaseAuthentication} if one was detected;
     *                                 null otherwise.
     * @return True if an authority could be determined; false otherwise.
     */
    public static boolean getAuthentication(
            final URI targetUri,
            final VsoTokenScope scope,
            final ICredentialStore personalAccessTokenStore,
            final ITokenStore adaRefreshTokenStore,
            final AtomicReference<BaseAuthentication> authentication)
    {
        throw new NotImplementedException();
    }
}
