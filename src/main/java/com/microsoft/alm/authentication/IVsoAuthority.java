package com.microsoft.alm.authentication;

import java.net.URI;
import java.util.concurrent.Future;

interface IVsoAuthority extends IAzureAuthority
{
    Future<Token> generatePersonalAccessToken(final URI targetUri, final Token accessToken, final VsoTokenScope tokenScope, final boolean requireCompactToken);
    Future<Boolean> validateCredentials(final URI targetUri, final Credential credentials);
    Future<Boolean> validateToken(final URI targetUri, final Token token);
}
