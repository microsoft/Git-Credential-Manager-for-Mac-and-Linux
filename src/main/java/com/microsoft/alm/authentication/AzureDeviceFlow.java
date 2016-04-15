// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.QueryString;

public class AzureDeviceFlow extends DeviceFlowImpl
{
    private String resource;

    public String getResource()
    {
        return resource;
    }

    public void setResource(final String resource)
    {
        this.resource = resource;
    }

    @Override
    protected void contributeAuthorizationRequestParameters(final QueryString bodyParameters)
    {
        if (resource != null)
        {
            bodyParameters.put("resource", resource);
        }
    }

    @Override
    protected DeviceFlowResponse buildDeviceFlowResponse(final String responseText)
    {
        return AzureDeviceFlowResponse.fromJson(responseText);
    }
}
