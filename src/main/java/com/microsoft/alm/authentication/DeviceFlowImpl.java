// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.HttpClient;
import com.microsoft.alm.helpers.QueryString;
import com.microsoft.alm.helpers.StringContent;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.oauth2.useragent.AuthorizationException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

public class DeviceFlowImpl implements DeviceFlow
{
    @Override
    public DeviceFlowResponse requestAuthorization(final URI deviceEndpoint, final String clientId, final String scope)
    {
        final QueryString bodyParameters = new QueryString();
        bodyParameters.put(OAuthParameter.RESPONSE_TYPE, OAuthParameter.DEVICE_CODE);
        bodyParameters.put(OAuthParameter.CLIENT_ID, clientId);
        if (!StringHelper.isNullOrEmpty(scope)) {
            bodyParameters.put(OAuthParameter.SCOPE, scope);
        }
        final StringContent requestBody = StringContent.createUrlEncoded(bodyParameters);

        final HttpClient client = new HttpClient(Global.getUserAgent());
        final String responseText;
        try {
            final HttpURLConnection response = client.post(deviceEndpoint, requestBody);
            final int httpStatus = response.getResponseCode();
            responseText = HttpClient.readToString(response);
            if (httpStatus != HttpURLConnection.HTTP_OK) {
                throw new Error("Device endpoint returned HTTP " + httpStatus + ":\n" + responseText);
            }
        }
        catch (final IOException e) {
            throw new Error(e);
        }

        final DeviceFlowResponse result = DeviceFlowResponse.fromJson(responseText);
        return result;
    }

    @Override
    public TokenPair requestToken(final URI tokenEndpoint, final String clientId, final DeviceFlowResponse deviceFlowResponse) throws AuthorizationException
    {
        return new TokenPair("d15281b1-03f1-4581-90d3-4527d9cf4147", "fake-refresh-token");
    }
}
