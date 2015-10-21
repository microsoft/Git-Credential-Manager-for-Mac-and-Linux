// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import java.net.URI;

interface IGithubAuthority
{
    GithubAuthenticationResult acquireToken(
        final URI targetUri,
        final String username,
        final String password,
        final String authenticationCode,
        final GithubTokenScope scope);

    boolean validateCredentials(final URI targetUri, final Credential credentials);
}
