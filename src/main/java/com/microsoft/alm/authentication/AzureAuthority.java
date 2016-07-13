// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Action;
import com.microsoft.alm.helpers.Debug;
import com.microsoft.alm.helpers.Guid;
import com.microsoft.alm.helpers.HttpClient;
import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.helpers.ObjectExtensions;
import com.microsoft.alm.helpers.QueryString;
import com.microsoft.alm.helpers.StringContent;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.helpers.Trace;
import com.microsoft.alm.helpers.UriHelper;
import com.microsoft.alm.oauth2.useragent.AuthorizationException;
import com.microsoft.alm.oauth2.useragent.AuthorizationResponse;
import com.microsoft.alm.oauth2.useragent.Provider;
import com.microsoft.alm.oauth2.useragent.ProviderScanner;
import com.microsoft.alm.oauth2.useragent.UserAgent;
import com.microsoft.alm.oauth2.useragent.UserAgentImpl;
import com.microsoft.alm.secret.Credential;
import com.microsoft.alm.secret.Token;
import com.microsoft.alm.secret.TokenPair;
import com.microsoft.alm.secret.TokenType;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        this(authorityHostUrl, new UserAgentImpl(), new AzureDeviceFlow());
    }

    AzureAuthority(final String authorityHostUrl, final UserAgent userAgent, final AzureDeviceFlow azureDeviceFlow)
    {
        Debug.Assert(UriHelper.isWellFormedUriString(authorityHostUrl), "The authorityHostUrl parameter is invalid.");
        Debug.Assert(userAgent != null, "The userAgent parameter is null.");

        this.authorityHostUrl = authorityHostUrl;
        _adalTokenCache = /* TODO: 449201: consider new InsecureStore("adalTokenCache.xml");*/null;
        _userAgent = userAgent;
        _azureDeviceFlow = azureDeviceFlow;
    }

    private final VsoAdalTokenCache _adalTokenCache;
    private final UserAgent _userAgent;
    private final AzureDeviceFlow _azureDeviceFlow;

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
    public TokenPair acquireToken(final URI targetUri, final String clientId, final String resource, final URI redirectUri, String queryParameters)
    {
        Debug.Assert(targetUri != null && targetUri.isAbsolute(), "The targetUri parameter is null or invalid");
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(clientId), "The clientId parameter is null or empty");
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(resource), "The resource parameter is null or empty");
        Debug.Assert(redirectUri != null, "The redirectUri parameter is null");
        Debug.Assert(redirectUri.isAbsolute(), "The redirectUri parameter is not an absolute Uri");

        Trace.writeLine("AzureAuthority::acquireToken");

        final UUID correlationId = null;
        TokenPair tokens = null;
        queryParameters = ObjectExtensions.coalesce(queryParameters, StringHelper.Empty);

        // TODO: 449243: check _adalTokenCache first, then attempt to acquire token from refresh token

        final String authorizationCode = acquireAuthorizationCode(resource, clientId, redirectUri, queryParameters);
        if (authorizationCode == null)
        {
            Trace.writeLine("   token acquisition failed.");
            return tokens;
        }

        final HttpClient client = new HttpClient(Global.getUserAgent());
        try
        {
            final URI tokenEndpoint = createTokenEndpointUri(authorityHostUrl);
            final StringContent requestContent = createTokenRequest(resource, clientId, authorizationCode, redirectUri, correlationId);
            final HttpURLConnection connection = client.post(tokenEndpoint, requestContent, new Action<HttpURLConnection>()
            {
                @Override public void call(final HttpURLConnection conn)
                {
                    conn.setUseCaches(false);
                }
            });
            client.ensureOK(connection);
            final String responseContent = HttpClient.readToString(connection);
            tokens = new TokenPair(responseContent);

            // TODO: 449201: store access + refresh tokens to _adalTokenCache

            Trace.writeLine("   token acquisition succeeded.");
        }
        catch (final IOException e)
        {
            throw new Error("   token acquisition failed.", e);
        }
        return tokens;
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
    public TokenPair acquireToken(final URI targetUri, final String clientId, final String resource, final Credential credentials)
    {
        throw new NotImplementedException(449285);
    }

    public TokenPair acquireToken(final URI targetUri, final String clientId, final String resource, final URI redirectUri, final Action<DeviceFlowResponse> callback)
    {
        Debug.Assert(targetUri != null && targetUri.isAbsolute(), "The targetUri parameter is null or invalid");
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(clientId), "The clientId parameter is null or empty");
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(resource), "The resource parameter is null or empty");
        Debug.Assert(callback != null, "The callback parameter is null");

        Trace.writeLine("AzureAuthority::acquireToken");

        _azureDeviceFlow.setResource(resource);
        _azureDeviceFlow.setRedirectUri(redirectUri);
        final StringBuilder sb = new StringBuilder(authorityHostUrl);
        sb.append("/oauth2/devicecode");
        final URI deviceEndpoint = URI.create(sb.toString());
        final DeviceFlowResponse response = _azureDeviceFlow.requestAuthorization(deviceEndpoint, clientId, null);

        callback.call(response);

        TokenPair tokens = null;
        final URI tokenEndpoint = createTokenEndpointUri(authorityHostUrl);
        try
        {
            tokens = _azureDeviceFlow.requestToken(tokenEndpoint, clientId, response);

            Trace.writeLine("   token acquisition succeeded.");
        }
        catch (final AuthorizationException e)
        {
            Trace.writeLine("   token acquisition failed: ", e);
        }
        return tokens;
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
    public TokenPair acquireTokenByRefreshToken(final URI targetUri, final String clientId, final String resource, final Token refreshToken)
    {
        throw new NotImplementedException(449243);
    }

    String acquireAuthorizationCode(final String resource, final String clientId, final URI redirectUri, final String queryParameters)
    {
        final String expectedState = UUID.randomUUID().toString();
        String authorizationCode = null;
        final String errorMessage = "Authorization code could not be obtained: ";
        try
        {
            final URI authorizationEndpoint = createAuthorizationEndpointUri(authorityHostUrl, resource, clientId, redirectUri, UserIdentifier.ANY_USER, expectedState, PromptBehavior.ALWAYS, queryParameters);
            final ProviderScanner providerScanner = (ProviderScanner) _userAgent;
            if (!providerScanner.hasCompatibleProvider())
            {
                final Map<Provider, List<String>> unmetRequirements = providerScanner.getUnmetProviderRequirements();
                final StringBuilder sb = new StringBuilder();
                UserAgentImpl.describeUnmetRequirements(unmetRequirements, sb);
                Trace.writeLine(sb.toString());
                return null;
            }
            final AuthorizationResponse response = _userAgent.requestAuthorizationCode(authorizationEndpoint, redirectUri);
            authorizationCode = response.getCode();
            // verify that the authorization response gave us the state we sent in the authz endpoint URI
            final String actualState = response.getState();
            if (!expectedState.equals(actualState))
            {
                // the states are somehow different; better to assume malice and ignore the authz code
                authorizationCode = null;
            }
        }
        catch (final AuthorizationException e)
        {
            Trace.writeLine(errorMessage, e);
        }
        return authorizationCode;
    }

    static URI createAuthorizationEndpointUri(final String authorityHostUrl, final String resource, final String clientId, final URI redirectUri, final UserIdentifier userId, final String state, final PromptBehavior promptBehavior, final String queryParameters)
    {
        final QueryString qs = new QueryString();
        qs.put(OAuthParameter.RESOURCE, resource);
        qs.put(OAuthParameter.CLIENT_ID, clientId);
        qs.put(OAuthParameter.RESPONSE_TYPE, OAuthParameter.CODE);
        qs.put(OAuthParameter.REDIRECT_URI, redirectUri.toString());

        if (!userId.isAnyUser()
            && (userId.getType() == UserIdentifierType.OPTIONAL_DISPLAYABLE_ID
                || userId.getType() == UserIdentifierType.REQUIRED_DISPLAYABLE_ID))
        {
            qs.put(OAuthParameter.LOGIN_HINT, userId.getId());
        }

        if (state != null)
        {
            qs.put(OAuthParameter.STATE, state);
        }

        String promptValue = null;
        switch (promptBehavior)
        {
            case ALWAYS:
                promptValue = PromptValue.LOGIN;
                break;
            case NEVER:
                promptValue = PromptValue.ATTEMPT_NONE;
                break;
            case REFRESH_SESSION:
                throw new NotImplementedException(449280, "implement when oauth2-useragent supports persistent cookies");
        }
        if (promptValue != null)
        {
            qs.put(OAuthParameter.PROMPT, promptValue);
        }

        final StringBuilder sb = new StringBuilder(authorityHostUrl);
        sb.append("/oauth2/authorize?");
        sb.append(qs.toString());
        if (!StringHelper.isNullOrWhiteSpace(queryParameters))
        {
            // TODO: 449282: ADAL.NET checks if queryParameters contains any duplicate parameters
            int start = (queryParameters.charAt(0) == '&') ? 1 : 0;
            sb.append('&').append(queryParameters, start, queryParameters.length());
        }
        final URI result;
        try
        {
            result = new URI(sb.toString());
        }
        catch (final URISyntaxException e)
        {
            throw new Error(e);
        }
        return result;
    }

    static URI createTokenEndpointUri(final String authorityHostUrl)
    {
        final StringBuilder sb = new StringBuilder(authorityHostUrl);
        sb.append("/oauth2/token");
        final URI result;
        try
        {
            result = new URI(sb.toString());
        }
        catch (final URISyntaxException e)
        {
            throw new Error(e);
        }
        return result;
    }

    static StringContent createTokenRequest(final String resource, final String clientId, final String authorizationCode, final URI redirectUri, final UUID correlationId)
    {
        final QueryString qs = new QueryString();
        qs.put(OAuthParameter.RESOURCE, resource);
        qs.put(OAuthParameter.CLIENT_ID, clientId);
        qs.put(OAuthParameter.GRANT_TYPE, OAuthParameter.AUTHORIZATION_CODE);
        qs.put(OAuthParameter.CODE, authorizationCode);
        qs.put(OAuthParameter.REDIRECT_URI, redirectUri.toString());
        if (correlationId != null && !Guid.Empty.equals(correlationId))
        {
            qs.put(OAuthParameter.CORRELATION_ID, correlationId.toString());
            qs.put(OAuthParameter.REQUEST_CORRELATION_ID_IN_RESPONSE, "true");
        }
        final StringContent result = StringContent.createUrlEncoded(qs);
        return result;
    }

    public static String getAuthorityUrl(final UUID tenantId)
    {
        return String.format("%1$s/%2$s", AuthorityHostUrlBase, tenantId.toString());
    }
}
