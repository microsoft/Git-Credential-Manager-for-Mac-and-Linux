// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.HttpClient;
import com.microsoft.alm.helpers.PropertyBag;
import com.microsoft.alm.helpers.QueryString;
import com.microsoft.alm.helpers.StringContent;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.oauth2.useragent.AuthorizationException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Calendar;

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
        contributeAuthorizationRequestParameters(bodyParameters);
        final StringContent requestBody = StringContent.createUrlEncoded(bodyParameters);

        final HttpClient client = new HttpClient(Global.getUserAgent());
        final String responseText;
        try {
            final HttpURLConnection response = client.post(deviceEndpoint, requestBody);
            final int httpStatus = response.getResponseCode();
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                responseText = HttpClient.readToString(response);
            }
            else {
                final String errorResponseText = HttpClient.readErrorToString(response);
                throw new Error("Device endpoint returned HTTP " + httpStatus + ":\n" + errorResponseText);
            }
        }
        catch (final IOException e) {
            throw new Error(e);
        }

        final DeviceFlowResponse result = buildDeviceFlowResponse(responseText);
        return result;
    }

    /**
     * Allows subclasses to augment the request to the device endpoint with additional parameters.
     *
     * @param bodyParameters the {@link QueryString} to which additional parameters should be added.
     */
    protected void contributeAuthorizationRequestParameters(final QueryString bodyParameters) {
        // do nothing by default
    }

    /**
     * Allows subclasses to construct a subclass of {@link DeviceFlowResponse} with extra metadata, etc.
     *
     * @param responseText the JSON response received from the device endpoint.
     *
     * @return             a {@link DeviceFlowResponse} (or subclass thereof).
     */
    protected DeviceFlowResponse buildDeviceFlowResponse(final String responseText) {
        return DeviceFlowResponse.fromJson(responseText);
    }

    @Override
    public TokenPair requestToken(final URI tokenEndpoint, final String clientId, final DeviceFlowResponse deviceFlowResponse) throws AuthorizationException
    {
        final QueryString bodyParameters = new QueryString();
        bodyParameters.put(OAuthParameter.GRANT_TYPE, OAuthParameter.DEVICE_CODE);
        bodyParameters.put(OAuthParameter.CODE, deviceFlowResponse.getDeviceCode());
        bodyParameters.put(OAuthParameter.CLIENT_ID, clientId);
        contributeTokenRequestParameters(bodyParameters);
        final StringContent requestBody = StringContent.createUrlEncoded(bodyParameters);

        final int intervalSeconds = deviceFlowResponse.getInterval();
        int intervalMilliseconds = intervalSeconds * 1000;
        final HttpClient client = new HttpClient(Global.getUserAgent());
        String responseText = null;
        final Calendar expiresAt = deviceFlowResponse.getExpiresAt();
        while (Calendar.getInstance().compareTo(expiresAt) <= 0) {
            try {
                final HttpURLConnection response = client.post(tokenEndpoint, requestBody);
                final int httpStatus = response.getResponseCode();
                if (httpStatus == HttpURLConnection.HTTP_OK) {
                    responseText = HttpClient.readToString(response);
                    break;
                }
                else {
                    final String errorResponseText = HttpClient.readErrorToString(response);
                    if (httpStatus == HttpURLConnection.HTTP_BAD_REQUEST) {
                        final PropertyBag bag = PropertyBag.fromJson(errorResponseText);
                        final String errorCode = bag.readOptionalString(OAuthParameter.ERROR_CODE, "unknown_error");
                        if (OAuthParameter.ERROR_AUTHORIZATION_PENDING.equals(errorCode)) {
                            try {
                                Thread.sleep(intervalMilliseconds);
                            } catch (final InterruptedException e) {
                                throw new Error(e);
                            }
                            continue;
                        }
                        else if (OAuthParameter.ERROR_SLOW_DOWN.equals(errorCode)) {
                            intervalMilliseconds *= 2;
                            try {
                                Thread.sleep(intervalMilliseconds);
                            } catch (final InterruptedException e) {
                                throw new Error(e);
                            }
                            continue;
                        }
                        final String errorDescription = bag.readOptionalString(OAuthParameter.ERROR_DESCRIPTION, null);
                        final String errorUriString = bag.readOptionalString(OAuthParameter.ERROR_URI, null);
                        final URI errorUri = errorUriString == null ? null : URI.create(errorUriString);
                        throw new AuthorizationException(errorCode, errorDescription, errorUri, null);
                    }
                    else {
                        throw new Error("Token endpoint returned HTTP " + httpStatus + ":\n" + errorResponseText);
                    }
                }
            }
            catch (final IOException e) {
                throw new Error(e);
            }
        }

        if (responseText == null) {
            throw new AuthorizationException("code_expired", "The verification code expired.", null, null);
        }
        final TokenPair tokenPair = buildTokenPair(responseText);
        return tokenPair;
    }

    /**
     * Allows subclasses to augment the request to the token endpoint with additional parameters.
     *
     * @param bodyParameters the {@link QueryString} to which additional parameters should be added.
     */
    protected void contributeTokenRequestParameters(final QueryString bodyParameters) {
        // do nothing by default
    }

    /**
     * Allows subclasses to construct a subclass of {@link TokenPair} with extra metadata, etc.
     *
     * @param responseText the JSON response received from the token endpoint.
     *
     * @return             a {@link TokenPair} (or subclass thereof).
     */
    protected TokenPair buildTokenPair(final String responseText) {
        final TokenPair tokenPair = new TokenPair(responseText);
        return tokenPair;
    }
}
