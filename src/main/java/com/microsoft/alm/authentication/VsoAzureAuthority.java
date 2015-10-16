// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Action;
import com.microsoft.alm.helpers.Debug;
import com.microsoft.alm.helpers.Environment;
import com.microsoft.alm.helpers.HttpClient;
import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.helpers.StringContent;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.helpers.Trace;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
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
        final String ContentJsonFormat = "{ \"scope\" : \"%1$s\", \"targetAccounts\" : [\"%2$s\"], \"displayName\" : \"Git: %3$s on %4$s\" }";

        Debug.Assert(accessToken != null && (accessToken.Type == TokenType.Access || accessToken.Type == TokenType.Federated), "The accessToken parameter is null or invalid");
        Debug.Assert(tokenScope != null, "The tokenScope parameter is null");

        final String targetIdentity = accessToken.getTargetIdentity().toString();
        Trace.writeLine("   creating access token scoped to '" + tokenScope + "' for '" + targetIdentity + "'");

        final String jsonContent = String.format(ContentJsonFormat, tokenScope, targetIdentity, targetUri, Environment.getMachineName());
        final StringContent content = StringContent.createJson(jsonContent);
        return content;
    }

    private HttpURLConnection createConnectionDataRequest(final URI targetUri, final Credential credentials) throws IOException
    {
        Debug.Assert(targetUri != null && targetUri.isAbsolute(), "The targetUri parameter is null or invalid");
        Debug.Assert(credentials != null, "The credentials parameter is null or invalid");

        final HttpClient client = new HttpClient(Global.getUserAgent());

        // create an request to the VSO deployment data end-point
        final URI requestUri = createConnectionDataUri(targetUri);

        credentials.contributeHeader(client.Headers);

        final HttpURLConnection result = client.get(requestUri, new Action<HttpURLConnection>()
        {
            @Override public void call(final HttpURLConnection conn)
            {
                conn.setConnectTimeout(RequestTimeout);
            }
        });
        return result;
    }

    private HttpURLConnection createConnectionDataRequest(final URI targetUri, final Token token) throws IOException
    {
        Debug.Assert(targetUri != null && targetUri.isAbsolute(), "The targetUri parameter is null or invalid");
        Debug.Assert(token != null && (token.Type == TokenType.Access || token.Type == TokenType.Federated), "The token parameter is null or invalid");

        Trace.writeLine("VsoAzureAuthority::createConnectionDataRequest");

        final HttpClient client = new HttpClient(Global.getUserAgent());

        // create an request to the VSO deployment data end-point
        final URI requestUri = createConnectionDataUri(targetUri);

        Trace.writeLine("   validating token");
        token.contributeHeader(client.Headers);

        final HttpURLConnection result = client.get(requestUri, new Action<HttpURLConnection>()
        {
            @Override public void call(final HttpURLConnection conn)
            {
                conn.setConnectTimeout(RequestTimeout);
            }
        });
        return result;
    }

    private URI createConnectionDataUri(final URI targetUri)
    {
        final String VsoValidationUrlFormat = "https://%1$s/_apis/connectiondata";

        Debug.Assert(targetUri != null & targetUri.isAbsolute(), "The targetUri parameter is null or invalid");

        // create a url to the connection data end-point, it's deployment level and "always on".
        final String validationUrl = String.format(VsoValidationUrlFormat, targetUri.getHost());

        final URI result = URI.create(validationUrl);
        return result;
    }
}
