package com.microsoft.alm.java.git_credential_helper.authentication;

import java.net.URI;
import java.util.concurrent.Future;

interface IGithubAuthority
{
    Future<GithubAuthenticationResult> acquireToken(
        final URI targetUri,
        final String username,
        final String password,
        final String authenticationCode,
        final GithubTokenScope scope);

    Future<Boolean> validateCredentials(final URI targetUri, final Credential credentials);
}
