// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication

import com.github.tomakehurst.wiremock.junit.WireMockRule
import groovy.transform.CompileStatic
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * A class to test {@see DeviceFlowImpl}.
 */
@CompileStatic
public class DeviceFlowImplTest {

    private static final String PROTOCOL = "http";
    private static final String CLIENT_ID = "contoso";
    private static final String DEVICE_CODE = "9297fb18-46d0-4846-97ca-ab8dd3b55729";
    private static final String USER_CODE = "A1B2B4C1C5D1D3E3E5";
    private static final String ACCESS_TOKEN = "d15281b1-03f1-4581-90d3-4527d9cf4147";
    private static final URI VERIFICATION_URI = new URI("http://verification.example.com");
    private static final int EXPIRY_SECONDS = 600;
    private static final int ATTEMPT_INTERVAL = 1;
    private static final String DEVICE_ENDPOINT_PATH = "/device";
    private static final String TOKEN_ENDPOINT_PATH = "/token";
    private static final DeviceFlowResponse DEFAULT_DEVICE_FLOW_RESPONSE = new DeviceFlowResponse(DEVICE_CODE, USER_CODE, VERIFICATION_URI, EXPIRY_SECONDS, ATTEMPT_INTERVAL);

    private final String host;
    private int deviceEndpointExpectedHits;
    private int tokenEndpointExpectedErrorHits;
    private int tokenEndpointExpectedSuccessHits;

    @Rule public WireMockRule wireMockRule = new WireMockRule(0);

    @Before public void initializeExpectedHits() {
        deviceEndpointExpectedHits = 0;
        tokenEndpointExpectedErrorHits = 0;
        tokenEndpointExpectedSuccessHits = 0;
    }

    @After public void verifyExpectedHits() {
        verify(deviceEndpointExpectedHits, postRequestedFor(urlEqualTo(DEVICE_ENDPOINT_PATH)));
        verify(tokenEndpointExpectedErrorHits + tokenEndpointExpectedSuccessHits, postRequestedFor(urlEqualTo(TOKEN_ENDPOINT_PATH)));
    }

    public DeviceFlowImplTest() {
        final def localHostAddress = InetAddress.localHost;
        host = localHostAddress.hostName;
    }

    private void stubDeviceEndpoint(final int interval = ATTEMPT_INTERVAL, final int expiresIn = -1, final String requestBodySuffix = "", String responseBodyPrefix = "") {
        final def deviceRequestBody = "response_type=device_code&client_id=${CLIENT_ID}" + requestBodySuffix;
        if (interval > 0) {
            responseBodyPrefix += /"interval":${interval},
/
        }
        if (expiresIn > 0) {
            responseBodyPrefix += /"expires_in":${expiresIn},
/
        }
        final def deviceResponseBody = """\
{
    ${responseBodyPrefix}
    "device_code":"${DEVICE_CODE}",
    "user_code":"${USER_CODE}",
    "verification_uri":"${VERIFICATION_URI}"
}
""";

        stubFor(
            post(
                urlEqualTo(DEVICE_ENDPOINT_PATH)
            )
            .withRequestBody(
                equalTo(deviceRequestBody)
            )
            .willReturn(
                aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withHeader("Cache-Control", "no-store")
                .withBody(deviceResponseBody)
            )
        );
        deviceEndpointExpectedHits++;
    }

    private void stubTokenEndpointSuccess(final String requestBodySuffix = "", String responseBodyPrefix = "") {
        final def tokenRequestBody = "grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}" + requestBodySuffix;
        final def tokenResponseBody = """\
{
    ${responseBodyPrefix}
    "access_token":"${ACCESS_TOKEN}",
    "token_type":"bearer",
    "expires_in":3600
}
""";
        stubFor(
            post(
                urlEqualTo(TOKEN_ENDPOINT_PATH)
            )
            .withRequestBody(
                equalTo(tokenRequestBody)
            )
            .willReturn(
                aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json;charset=UTF-8")
                .withHeader("Cache-Control", "no-store")
                .withHeader("Pragma", "no-cache")
                .withBody(tokenResponseBody)
            )
        );
        tokenEndpointExpectedSuccessHits++;
    }

    private void stubTokenEndpointError(final String requestBody, final String errorCode, final String errorDescription = null, final URI errorUri = null) {

        final def tokenRequestBody = requestBody;

        def responseBodyPrefix = ""
        if (errorDescription) {
            responseBodyPrefix += /"error_description": "${errorDescription}",
/
        }
        if (errorUri != null) {
            responseBodyPrefix += /"error_uri": "${errorUri}",
/
        }

        final def tokenResponseBody = """\
{
    ${responseBodyPrefix}
    "error":"${errorCode}"
}
""";

        stubFor(
            post(
                urlEqualTo(TOKEN_ENDPOINT_PATH)
            )
            .withRequestBody(
                equalTo(tokenRequestBody)
            )
            .willReturn(
                aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json;charset=UTF-8")
                .withHeader("Cache-Control", "no-store")
                .withHeader("Pragma", "no-cache")
                .withBody(tokenResponseBody)
            )
        );
        tokenEndpointExpectedErrorHits++;
    }

    @Test public void endToEnd_authorizedRightAway() {
        final def port = wireMockRule.port();
        final def deviceEndpoint = new URI(PROTOCOL, null, host, port, DEVICE_ENDPOINT_PATH, null, null);
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        final def cut = new DeviceFlowImpl();

        final def actualResponse = cut.requestAuthorization(deviceEndpoint, CLIENT_ID, null);

        assert DEVICE_CODE == actualResponse.deviceCode;
        assert USER_CODE == actualResponse.userCode;
        assert VERIFICATION_URI == actualResponse.verificationUri;
        assert EXPIRY_SECONDS == actualResponse.expiresIn;
        assert ATTEMPT_INTERVAL == actualResponse.interval;

        final def actualTokenPair = cut.requestToken(tokenEndpoint, CLIENT_ID, actualResponse);

        final def actualAccessToken = actualTokenPair.AccessToken;
        assert TokenType.Access == actualAccessToken.Type;
        assert ACCESS_TOKEN == actualAccessToken.Value;
    }

}
