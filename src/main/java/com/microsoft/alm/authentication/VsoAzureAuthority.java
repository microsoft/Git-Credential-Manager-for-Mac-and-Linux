package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.helpers.StringContent;

import java.net.URI;
import java.util.concurrent.Future;

class VsoAzureAuthority extends AzureAuthority implements IVsoAuthority
{
    /**
     * The maximum wait time for a network request before timing out
     */
    public static final int RequestTimeout = 15 * 1000; // 15 second limit

    public VsoAzureAuthority() { this (null); }
    public VsoAzureAuthority(final String authorityHostUrl)
    {
        super();
        if (authorityHostUrl != null)
        {
            this.authorityHostUrl = authorityHostUrl;
        }
    }

    /**
     * Generates a personal access token for use with Visual Studio Online.
     *
     * @param targetUri           The uniform resource indicator of the resource access tokens are being requested for.
     * @param accessToken
     * @param tokenScope
     * @param requireCompactToken
     * @return
     */
    @Override public Future<Token> generatePersonalAccessToken(final URI targetUri, final Token accessToken, final VsoTokenScope tokenScope, final boolean requireCompactToken)
    {
        throw new NotImplementedException();
    }

    public Future<Boolean> populateTokenTargetId(final URI targetUri, final Token accessToken)
    {
        throw new NotImplementedException();
    }

    /**
     * Validates that {@link Credential} are valid to grant access to the Visual Studio
     * Online service represented by the {@literal targetUri} parameter.
     *
     * @param targetUri   Uniform resource identifier for a VSO service.
     * @param credentials {@link Credential} expected to grant access to the VSO service.
     * @return True if successful; otherwise false.
     */
    @Override public Future<Boolean> validateCredentials(final URI targetUri, final Credential credentials)
    {
        throw new NotImplementedException();
    }

    /**
     * <p>Validates that {@link Token} are valid to grant access to the Visual Studio
     * Online service represented by the {@literal targetUri} parameter.</p>
     * <p>Tokens of {@link TokenType#Refresh} cannot grant access, and
     * therefore always fail - this does not mean the token is invalid.</p>
     *
     * @param targetUri   Uniform resource identifier for a VSO service.
     * @param token       {@link Token} expected to grant access to the VSO service.
     * @return True if successful; otherwise false.
     */
    @Override public Future<Boolean> validateToken(final URI targetUri, final Token token)
    {
        throw new NotImplementedException();
    }

    private StringContent getAccessTokenRequestBody(final URI targetUri, final Token accessToken, final VsoTokenScope tokenScope)
    {
        throw new NotImplementedException();
    }

    private Object /* TODO: HttpWebRequest*/ getConnectionDataRequest(final URI targetUri, final Credential credentials)
    {
        throw new NotImplementedException();
    }

    private Object /* TODO: HttpWebRequest*/ getConnectionDataRequest(final URI targetUri, final Token token)
    {
        throw new NotImplementedException();
    }

    private Object /* TODO: HttpWebRequest*/ getConnectionDataRequest(final URI targetUri)
    {
        throw new NotImplementedException();
    }
}
