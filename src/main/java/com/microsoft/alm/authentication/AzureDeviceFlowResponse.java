// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.PropertyBag;

import java.net.URI;

public class AzureDeviceFlowResponse extends DeviceFlowResponse
{
    static final String VERIFICATION_URL = "verification_url";
    static final String MESSAGE = "message";

    private final String message;

    public AzureDeviceFlowResponse(final String deviceCode, final String userCode, final URI verificationUri, final int expiresIn, final int interval, final String message)
    {
        super(deviceCode, userCode, verificationUri, expiresIn, interval);
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

    public static AzureDeviceFlowResponse fromJson(final String jsonText) {
        final PropertyBag bag = PropertyBag.fromJson(jsonText);
        final String deviceCode = (String) bag.get(OAuthParameter.DEVICE_CODE);
        final String userCode = (String) bag.get(OAuthParameter.USER_CODE);
        final String verificationUriString = (String) bag.get(VERIFICATION_URL);
        final URI verificationUri = URI.create(verificationUriString);
        final int expiresInSeconds = bag.readOptionalInteger(OAuthParameter.EXPIRES_IN, 600);
        final int intervalInSeconds = bag.readOptionalInteger(OAuthParameter.INTERVAL, 5);
        final String message = (String) bag.get(MESSAGE);

        final AzureDeviceFlowResponse result = new AzureDeviceFlowResponse(deviceCode, userCode, verificationUri, expiresInSeconds, intervalInSeconds, message);
        return result;
    }
}
