// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Action;
import com.microsoft.alm.helpers.NullUserAgent;
import com.microsoft.alm.helpers.StringContent;
import com.microsoft.alm.oauth2.useragent.AuthorizationException;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class AzureAuthorityTest
{
    static final String TEST_RESOURCE = "TEST_RESOURCE";
    static final String TEST_CLIENT_ID = "d30feefe-9ee4-4b00-ac77-08dbd1199811";
    static final String TEST_DEVICE_CODE = "03d5f4b1-c8ab-4ce2-85c0-158d2075ff5f";
    static final String TEST_USER_CODE = "DEADBEEF";
    static final URI TEST_REDIRECT_URI = URI.create("https://terminus.example.com");
    static final int TEST_EXPIRATION = 600;
    static final int TEST_INTERVAL = 5;
    static final String TEST_ACCESS_TOKEN = "bacf8b5f-63f2-4998-9170-d32cf7db4a78";
    static final String TEST_REFRESH_TOKEN = "c2be2d76-1e9e-487c-9684-78823747391c";

    @Test
    public void deviceFlow_success() throws Exception
    {
        final String authorityHostUrl = "https://authorization.example.com/common/";
        final URI targetUri = URI.create("https://resource.example.com/");
        final URI verificationUri = URI.create("https://authorization.example.com/oauth/device");
        final AtomicInteger requestAuthorizationCalls = new AtomicInteger(0);
        final AtomicInteger requestTokenCalls = new AtomicInteger(0);
        final AtomicInteger callbackCalls = new AtomicInteger(0);
        final AzureDeviceFlowResponse azureDeviceFlowResponse = new AzureDeviceFlowResponse(TEST_DEVICE_CODE, TEST_USER_CODE, verificationUri, TEST_EXPIRATION, TEST_INTERVAL, "message");
        final AzureDeviceFlow testDeviceFlow = new AzureDeviceFlow()
        {
            @Override public DeviceFlowResponse requestAuthorization(final URI deviceEndpoint, final String clientId, final String scope)
            {
                requestAuthorizationCalls.addAndGet(1);
                Assert.assertEquals(TEST_CLIENT_ID, clientId);
                return azureDeviceFlowResponse;
            }

            @Override public TokenPair requestToken(final URI tokenEndpoint, final String clientId, final DeviceFlowResponse deviceFlowResponse) throws AuthorizationException
            {
                requestTokenCalls.addAndGet(1);
                Assert.assertEquals(TEST_CLIENT_ID, clientId);
                Assert.assertEquals(azureDeviceFlowResponse, deviceFlowResponse);
                return new TokenPair(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
            }
        };
        final Action<DeviceFlowResponse> callback = new Action<DeviceFlowResponse>()
        {
            @Override public void call(final DeviceFlowResponse deviceFlowResponse)
            {
                callbackCalls.addAndGet(1);
                Assert.assertEquals(azureDeviceFlowResponse, deviceFlowResponse);
            }
        };
        final AzureAuthority cut = new AzureAuthority(authorityHostUrl, NullUserAgent.INSTANCE, testDeviceFlow);

        final TokenPair actualTokenPair = cut.acquireToken(targetUri, TEST_CLIENT_ID, TEST_RESOURCE, TEST_REDIRECT_URI, callback);

        Assert.assertEquals(TEST_ACCESS_TOKEN, actualTokenPair.AccessToken.Value);
        Assert.assertEquals(TEST_REFRESH_TOKEN, actualTokenPair.RefreshToken.Value);
        Assert.assertEquals(TEST_RESOURCE, testDeviceFlow.getResource());
        Assert.assertEquals(TEST_REDIRECT_URI, testDeviceFlow.getRedirectUri());
        Assert.assertEquals(1, requestAuthorizationCalls.get());
        Assert.assertEquals(1, requestTokenCalls.get());
        Assert.assertEquals(1, callbackCalls.get());
    }

    @Test
    public void deviceFlow_failure() throws Exception
    {
        final String authorityHostUrl = "https://authorization.example.com/common/";
        final URI targetUri = URI.create("https://resource.example.com/");
        final URI verificationUri = URI.create("https://authorization.example.com/oauth/device");
        final AtomicInteger requestAuthorizationCalls = new AtomicInteger(0);
        final AtomicInteger requestTokenCalls = new AtomicInteger(0);
        final AtomicInteger callbackCalls = new AtomicInteger(0);
        final AzureDeviceFlowResponse azureDeviceFlowResponse = new AzureDeviceFlowResponse(TEST_DEVICE_CODE, TEST_USER_CODE, verificationUri, TEST_EXPIRATION, TEST_INTERVAL, "message");
        final AzureDeviceFlow testDeviceFlow = new AzureDeviceFlow()
        {
            @Override public DeviceFlowResponse requestAuthorization(final URI deviceEndpoint, final String clientId, final String scope)
            {
                requestAuthorizationCalls.addAndGet(1);
                Assert.assertEquals(TEST_CLIENT_ID, clientId);
                return azureDeviceFlowResponse;
            }

            @Override public TokenPair requestToken(final URI tokenEndpoint, final String clientId, final DeviceFlowResponse deviceFlowResponse) throws AuthorizationException
            {
                requestTokenCalls.addAndGet(1);
                throw new AuthorizationException("access_denied");
            }
        };
        final Action<DeviceFlowResponse> callback = new Action<DeviceFlowResponse>()
        {
            @Override public void call(final DeviceFlowResponse deviceFlowResponse)
            {
                callbackCalls.addAndGet(1);
                Assert.assertEquals(azureDeviceFlowResponse, deviceFlowResponse);
            }
        };
        final AzureAuthority cut = new AzureAuthority(authorityHostUrl, NullUserAgent.INSTANCE, testDeviceFlow);

        final TokenPair actualTokenPair = cut.acquireToken(targetUri, TEST_CLIENT_ID, TEST_RESOURCE, TEST_REDIRECT_URI, callback);

        Assert.assertEquals(null, actualTokenPair);
        Assert.assertEquals(TEST_RESOURCE, testDeviceFlow.getResource());
        Assert.assertEquals(TEST_REDIRECT_URI, testDeviceFlow.getRedirectUri());
        Assert.assertEquals(1, requestAuthorizationCalls.get());
        Assert.assertEquals(1, requestTokenCalls.get());
        Assert.assertEquals(1, callbackCalls.get());
    }

    @Test
    public void createAuthorizationEndpointUri_minimal() throws Exception
    {
        final URI redirectUri = URI.create("https://example.com");
        final URI actual = AzureAuthority.createAuthorizationEndpointUri(
                "https://login.microsoftonline.com/common",
                "a8860e8f-ca7d-4efe-b80d-4affab13d4ba", "f7e11bcd-b50b-4869-ad88-8bdd6cbc8473",
                redirectUri,
                UserIdentifier.ANY_USER,
                null,
                PromptBehavior.AUTO,
                null);

        Assert.assertEquals("https://login.microsoftonline.com/common/oauth2/authorize?resource=a8860e8f-ca7d-4efe-b80d-4affab13d4ba&client_id=f7e11bcd-b50b-4869-ad88-8bdd6cbc8473&response_type=code&redirect_uri=https%3A%2F%2Fexample.com", actual.toString());
    }

    @Test
    public void createAuthorizationEndpointUri_typical() throws Exception
    {
        final URI redirectUri = URI.create("https://example.com");
        final String state = "519a4fa6-c18f-4230-8290-6c57407656c9";
        final URI actual = AzureAuthority.createAuthorizationEndpointUri(
                "https://login.microsoftonline.com/common",
                "a8860e8f-ca7d-4efe-b80d-4affab13d4ba", "f7e11bcd-b50b-4869-ad88-8bdd6cbc8473",
                redirectUri,
                UserIdentifier.ANY_USER,
                state,
                PromptBehavior.ALWAYS,
                null);

        Assert.assertEquals("https://login.microsoftonline.com/common/oauth2/authorize?resource=a8860e8f-ca7d-4efe-b80d-4affab13d4ba&client_id=f7e11bcd-b50b-4869-ad88-8bdd6cbc8473&response_type=code&redirect_uri=https%3A%2F%2Fexample.com&state=519a4fa6-c18f-4230-8290-6c57407656c9&prompt=login", actual.toString());
    }

    @Test
    public void createAuthorizationEndpointUri_extraState() throws Exception
    {
        final URI redirectUri = URI.create("https://example.com");
        final String state = "519a4fa6-c18f-4230-8290-6c57407656c9";
        final URI actual = AzureAuthority.createAuthorizationEndpointUri(
                "https://login.microsoftonline.com/common",
                "a8860e8f-ca7d-4efe-b80d-4affab13d4ba", "f7e11bcd-b50b-4869-ad88-8bdd6cbc8473",
                redirectUri,
                UserIdentifier.ANY_USER,
                state,
                PromptBehavior.ALWAYS,
                "state=bliss");

        Assert.assertEquals("https://login.microsoftonline.com/common/oauth2/authorize?resource=a8860e8f-ca7d-4efe-b80d-4affab13d4ba&client_id=f7e11bcd-b50b-4869-ad88-8bdd6cbc8473&response_type=code&redirect_uri=https%3A%2F%2Fexample.com&state=519a4fa6-c18f-4230-8290-6c57407656c9&prompt=login&state=bliss", actual.toString());
    }

    @Test
    public void createTokenEndpointUri_typical() throws Exception
    {
        final URI actual = AzureAuthority.createTokenEndpointUri("https://login.example.com/common");

        Assert.assertEquals("https://login.example.com/common/oauth2/token", actual.toString());
    }

    @Test
    public void createTokenRequest_typical() throws Exception
    {
        final String resource = "a8860e8f-ca7d-4efe-b80d-4affab13d4ba";
        final String clientId = "f7e11bcd-b50b-4869-ad88-8bdd6cbc8473";
        // authorization codes can be pretty long
        final String authorizationCode =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        final URI redirectUri = URI.create("https://example.com");

        final StringContent actual = AzureAuthority.createTokenRequest(resource, clientId, authorizationCode, redirectUri, null);

        Assert.assertEquals(
            "resource=a8860e8f-ca7d-4efe-b80d-4affab13d4ba" +
            "&client_id=f7e11bcd-b50b-4869-ad88-8bdd6cbc8473" +
            "&grant_type=authorization_code" +
            "&code=" + authorizationCode +
            "&redirect_uri=https%3A%2F%2Fexample.com", actual.getContent());
    }

    @Test
    public void createTokenRequest_withCorrelationId() throws Exception
    {
        final String resource = "a8860e8f-ca7d-4efe-b80d-4affab13d4ba";
        final String clientId = "f7e11bcd-b50b-4869-ad88-8bdd6cbc8473";
        final UUID correlationId = UUID.fromString("519a4fa6-c18f-4230-8290-6c57407656c9");
        // authorization codes can be pretty long
        final String authorizationCode =
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        final URI redirectUri = URI.create("https://example.com");

        final StringContent actual = AzureAuthority.createTokenRequest(resource, clientId, authorizationCode, redirectUri, correlationId);

        Assert.assertEquals(
            "resource=a8860e8f-ca7d-4efe-b80d-4affab13d4ba" +
                "&client_id=f7e11bcd-b50b-4869-ad88-8bdd6cbc8473" +
                "&grant_type=authorization_code" +
                "&code=" + authorizationCode +
                "&redirect_uri=https%3A%2F%2Fexample.com" +
                "&client-request-id=519a4fa6-c18f-4230-8290-6c57407656c9" +
                "&return-client-request-id=true", actual.getContent());
    }
}
