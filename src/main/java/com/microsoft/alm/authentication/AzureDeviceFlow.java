// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.QueryString;

import java.net.URI;

public class AzureDeviceFlow extends DeviceFlowImpl
{
    private String resource;
    private URI redirectUri;

    public String getResource()
    {
        return resource;
    }

    public void setResource(final String resource)
    {
        this.resource = resource;
    }

    public URI getRedirectUri()
    {
        return redirectUri;
    }

    public void setRedirectUri(final URI redirectUri)
    {
        this.redirectUri = redirectUri;
    }

    @Override
    protected void contributeAuthorizationRequestParameters(final QueryString bodyParameters)
    {
        if (resource != null)
        {
            bodyParameters.put("resource", resource);
        }

        if (redirectUri != null)
        {
            bodyParameters.put(OAuthParameter.REDIRECT_URI, redirectUri.toString());
        }
    }

    @Override
    protected DeviceFlowResponse buildDeviceFlowResponse(final String responseText)
    {
        return AzureDeviceFlowResponse.fromJson(responseText);
    }
}
