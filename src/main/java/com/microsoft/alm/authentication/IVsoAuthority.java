// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.secret.Credential;
import com.microsoft.alm.secret.Token;
import com.microsoft.alm.secret.VsoTokenScope;

import java.net.URI;

interface IVsoAuthority extends IAzureAuthority
{
    Token generatePersonalAccessToken(final URI targetUri, final Token accessToken, final VsoTokenScope tokenScope, final boolean requireCompactToken);
    boolean validateCredentials(final URI targetUri, final Credential credentials);
    boolean validateToken(final URI targetUri, final Token token);
}
