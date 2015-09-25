package com.microsoft.alm.authentication;

import java.net.URI;
import java.util.concurrent.Future;

interface IAzureAuthority
{
    TokenPair acquireToken(final URI targetUri, final String clientId, final String resource, final URI redirectUri, final String queryParameters);
    Future<TokenPair> acquireTokenAsync(final URI targetUri, final String clientId, final String resource, final Credential credentials);
    Future<TokenPair> acquireTokenByRefreshTokenAsync(final URI targetUri, final String clientId, final String resource, final Token refreshToken);
}
