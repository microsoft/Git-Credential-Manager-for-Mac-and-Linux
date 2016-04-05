// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.oauth2.useragent.AuthorizationException;

import java.net.URI;

public class DeviceFlowImpl implements DeviceFlow
{
    @Override
    public DeviceFlowResponse requestAuthorization(final URI deviceEndpoint, final String clientId, final String scope)
    {
        return new DeviceFlowResponse("9297fb18-46d0-4846-97ca-ab8dd3b55729", "A1B2B4C1C5D1D3E3E5", URI.create("http://verification.example.com"), 600, 1);
    }

    @Override
    public TokenPair requestToken(final URI tokenEndpoint, final String clientId, final DeviceFlowResponse deviceFlowResponse) throws AuthorizationException
    {
        return new TokenPair("d15281b1-03f1-4581-90d3-4527d9cf4147", "fake-refresh-token");
    }
}
