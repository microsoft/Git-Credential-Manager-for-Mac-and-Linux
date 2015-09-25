// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

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
