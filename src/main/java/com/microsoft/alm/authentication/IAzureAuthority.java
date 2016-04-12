// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Action;

import java.net.URI;

interface IAzureAuthority
{
    TokenPair acquireToken(final URI targetUri, final String clientId, final String resource, final URI redirectUri, final String queryParameters);
    TokenPair acquireToken(final URI targetUri, final String clientId, final String resource, final Credential credentials);
    TokenPair acquireToken(final URI targetUri, final String clientId, final String resource, final Action<DeviceFlowResponse> callback);
    TokenPair acquireTokenByRefreshToken(final URI targetUri, final String clientId, final String resource, final Token refreshToken);
}
