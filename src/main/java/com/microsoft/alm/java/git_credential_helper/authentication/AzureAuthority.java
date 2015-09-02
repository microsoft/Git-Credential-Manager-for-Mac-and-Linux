package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Interfaces with Azure to perform authentication and identity services.
 */
class AzureAuthority implements IAzureAuthority
{
    /**
     * The base URL for logon services in Azure.
     */
    public static final String AuthorityHostUrlBase = "https://login.microsoftonline.com";
    /**
     * The common Url for logon services in Azure.
     */
    public static final String DefaultAuthorityHostUrl = AuthorityHostUrlBase + "/common";

    /**
     * Creates a new {@link AzureAuthority} with the default authority host url.
     */
    public AzureAuthority() { this(DefaultAuthorityHostUrl); }

    /**
     * Creates a new {@link AzureAuthority} with an authority host url.
     *
     * @param authorityHostUrl Non-default authority host url.
     */
    public AzureAuthority(final String authorityHostUrl)
    {
        throw new NotImplementedException();
    }

    // TODO: private final VsoAdalTokenCache _adalTokenCache;

    protected String authorityHostUrl;
    /**
     * The URL used to interact with the Azure identity service.
     */
    public String getAuthorityHostUrl() { return authorityHostUrl; }

    /**
     * Acquires a {@link TokenPair} from the authority via an interactive user logon
     * prompt.
     *
     * @param targetUri       The uniform resource indicator of the resource access tokens are being requested for.
     * @param clientId        Identifier of the client requesting the token.
     * @param resource        Identifier of the target resource that is the recipient of the requested token.
     * @param redirectUri     Address to return to upon receiving a response from the authority.
     * @param queryParameters Optional: appended as-is to the query string in the HTTP authentication request to the
     *                        authority.
     * @return If successful, a {@link TokenPair}; otherwise null.
     */
    public TokenPair acquireToken(final URI targetUri, final String clientId, final String resource, final URI redirectUri, final String queryParameters)
    {
        throw new NotImplementedException();
    }


    /**
     * Acquires a {@link TokenPair} from the authority using optionally provided
     * credentials or via the current identity.
     *
     * @param targetUri   The uniform resource indicator of the resource access tokens are being requested for.
     * @param clientId    Identifier of the client requesting the token.
     * @param resource    Identifier of the target resource that is the recipient of the requested token.
     * @param credentials Optional: user credential to use for token acquisition.
     * @return If successful, a {@link TokenPair}; otherwise null.
     */
    public Future<TokenPair> acquireTokenAsync(final URI targetUri, final String clientId, final String resource, final Credential credentials)
    {
        throw new NotImplementedException();
    }

    /**
     * Acquires an access token from the authority using a previously acquired refresh token.
     *
     * @param targetUri    The uniform resource indicator of the resource access tokens are being requested for.
     * @param clientId     Identifier of the client requesting the token.
     * @param resource     Identifier of the target resource that is the recipient of the requested token.
     * @param refreshToken The {@link Token} of type {@link TokenType#Refresh}.
     * @return If successful, a {@link TokenPair}; otherwise null.
     */
    public Future<TokenPair> acquireTokenByRefreshTokenAsync(final URI targetUri, final String clientId, final String resource, final Token refreshToken)
    {
        throw new NotImplementedException();
    }

    public static String getAuthorityUrl(final UUID tenantId)
    {
        throw new NotImplementedException();
    }
}
