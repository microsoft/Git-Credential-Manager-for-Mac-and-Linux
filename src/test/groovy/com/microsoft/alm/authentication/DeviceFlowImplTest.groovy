// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication

import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.RequestListener
import com.github.tomakehurst.wiremock.http.Response
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.microsoft.alm.oauth2.useragent.AuthorizationException
import groovy.transform.CompileStatic
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.littleshoot.proxy.HttpProxyServer
import org.littleshoot.proxy.impl.DefaultHttpProxyServer

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
    private static final String SCENARIO = "Default stateful scenario";
    private static final String ALL_INTERFACES_ADDRESS = "0.0.0.0";
    private static final InetSocketAddress PROXY_LISTEN_ADDRESS = new InetSocketAddress(ALL_INTERFACES_ADDRESS, 0);


    private final String host;
    private String scenarioStateName;
    private int scenarioNextStateNumber;
    private int deviceEndpointExpectedHits;
    private int tokenEndpointExpectedErrorHits;
    private int tokenEndpointExpectedSuccessHits;

    @Rule public WireMockRule wireMockRule = new WireMockRule(0);

    @Before public void initializeExpectedHits() {
        scenarioStateName = Scenario.STARTED;
        scenarioNextStateNumber = 0;
        deviceEndpointExpectedHits = 0;
        tokenEndpointExpectedErrorHits = 0;
        tokenEndpointExpectedSuccessHits = 0;
    }

    @After public void verifyExpectedHits() {
        if (deviceEndpointExpectedHits > 0) {
            verify(deviceEndpointExpectedHits, postRequestedFor(urlEqualTo(DEVICE_ENDPOINT_PATH)));
        }
        if (tokenEndpointExpectedErrorHits + tokenEndpointExpectedSuccessHits > 0) {
            verify(tokenEndpointExpectedErrorHits + tokenEndpointExpectedSuccessHits, postRequestedFor(urlEqualTo(TOKEN_ENDPOINT_PATH)));
        }
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

        final def nextStateName = Integer.toString(scenarioNextStateNumber, 10);
        stubFor(
            post(
                urlEqualTo(DEVICE_ENDPOINT_PATH)
            )
            .inScenario(SCENARIO)
            .whenScenarioStateIs(scenarioStateName)
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
            .willSetStateTo(nextStateName)
        );
        deviceEndpointExpectedHits++;
        scenarioStateName = nextStateName;
        scenarioNextStateNumber++;
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

        final def nextStateName = Integer.toString(scenarioNextStateNumber, 10);
        stubFor(
            post(
                urlEqualTo(TOKEN_ENDPOINT_PATH)
            )
            .inScenario(SCENARIO)
            .whenScenarioStateIs(scenarioStateName)
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
            .willSetStateTo(nextStateName)
        );
        tokenEndpointExpectedSuccessHits++;
        scenarioStateName = nextStateName;
        scenarioNextStateNumber++;
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

        final def nextStateName = Integer.toString(scenarioNextStateNumber, 10);
        stubFor(
            post(
                urlEqualTo(TOKEN_ENDPOINT_PATH)
            )
            .inScenario(SCENARIO)
            .whenScenarioStateIs(scenarioStateName)
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
            .willSetStateTo(nextStateName)
        );
        tokenEndpointExpectedErrorHits++;
        scenarioStateName = nextStateName;
        scenarioNextStateNumber++;
    }

    @Test public void requestAuthorization_withScope() {
        final def port = wireMockRule.port();
        final def deviceEndpoint = new URI(PROTOCOL, null, host, port, DEVICE_ENDPOINT_PATH, null, null);
        stubDeviceEndpoint(ATTEMPT_INTERVAL, -1, "&scope=access_all_the_things")
        final def cut = new DeviceFlowImpl();

        final actualResponse = cut.requestAuthorization(deviceEndpoint, CLIENT_ID, "access_all_the_things")

        assert DEVICE_CODE == actualResponse.deviceCode;
        assert USER_CODE == actualResponse.userCode;
        assert VERIFICATION_URI == actualResponse.verificationUri;
        assert EXPIRY_SECONDS == actualResponse.expiresIn;
        assert ATTEMPT_INTERVAL == actualResponse.interval;
    }

    @Test public void requestAuthorization_serverError() {
        final def port = wireMockRule.port();
        final def deviceEndpoint = new URI(PROTOCOL, null, host, port, DEVICE_ENDPOINT_PATH, null, null);
        stubFor(post(urlEqualTo(DEVICE_ENDPOINT_PATH))
                .willReturn(aResponse()
                .withStatus(500)
                .withBody("Internal server error!")));
        final def cut = new DeviceFlowImpl();

        try {
            cut.requestAuthorization(deviceEndpoint, CLIENT_ID, null)
        }
        catch (final Error e) {
            final def actual = e.message.trim()
            assert "Device endpoint returned HTTP 500:\nInternal server error!" == actual;
            return;
        }
        Assert.fail("An Error should have been thrown");
    }

    @Test public void requestToken_serverError() {
        final def port = wireMockRule.port();
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        stubFor(post(urlEqualTo(TOKEN_ENDPOINT_PATH))
                .willReturn(aResponse()
                .withStatus(500)
                .withBody("Internal server error!")));
        final def cut = new DeviceFlowImpl();

        try {
            cut.requestToken(tokenEndpoint, CLIENT_ID, DEFAULT_DEVICE_FLOW_RESPONSE)
        }
        catch (final Error e) {
            final def actual = e.message.trim()
            assert "Token endpoint returned HTTP 500:\nInternal server error!" == actual;
            return;
        }
        Assert.fail("An Error should have been thrown");
    }

    @Test public void requestToken_waitsBetweenRequests() {
        final def port = wireMockRule.port();
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        final intervalMilliseconds = ATTEMPT_INTERVAL * 1000;
        final listener = new RequestListener() {
            private Calendar lastRequestTime = null;
            @Override void requestReceived(final Request request, final Response response) {
                final currentRequestTime = Calendar.instance;
                if (lastRequestTime != null) {
                    final currentRequestMilliseconds = currentRequestTime.timeInMillis;
                    final lastRequestMilliseconds = lastRequestTime.timeInMillis;
                    assert currentRequestMilliseconds - lastRequestMilliseconds >= intervalMilliseconds;
                }
                lastRequestTime = currentRequestTime;
            }
        };
        wireMockRule.addMockServiceRequestListener(listener);
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "authorization_pending");
        stubTokenEndpointSuccess();
        final def cut = new DeviceFlowImpl();

        final def actualTokenPair = cut.requestToken(tokenEndpoint, CLIENT_ID, DEFAULT_DEVICE_FLOW_RESPONSE);

        final def actualAccessToken = actualTokenPair.AccessToken;
        assert TokenType.Access == actualAccessToken.Type;
        assert ACCESS_TOKEN == actualAccessToken.Value;
    }

    @Test public void requestToken_givesUpWhenCodeExpires() {
        final def port = wireMockRule.port();
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "authorization_pending");
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "authorization_pending");
        final def cut = new DeviceFlowImpl();

        final def response = new DeviceFlowResponse(DEVICE_CODE, USER_CODE, VERIFICATION_URI, 2, 1);

        try {
            cut.requestToken(tokenEndpoint, CLIENT_ID, response)
        }
        catch (final AuthorizationException e) {
            assert "code_expired" == e.code;
            return;
        }
        Assert.fail("An AuthorizationException should have been thrown");
    }

    @Test public void requestToken_backsOff() {
        final testStartTime = Calendar.instance;
        final def port = wireMockRule.port();
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "authorization_pending");
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "slow_down");
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "slow_down");
        stubTokenEndpointSuccess();
        final def cut = new DeviceFlowImpl();

        final def actualTokenPair = cut.requestToken(tokenEndpoint, CLIENT_ID, DEFAULT_DEVICE_FLOW_RESPONSE);

        final def actualAccessToken = actualTokenPair.AccessToken;
        assert TokenType.Access == actualAccessToken.Type;
        assert ACCESS_TOKEN == actualAccessToken.Value;
        final testEndTime = Calendar.instance;
        assert testEndTime.timeInMillis - testStartTime.timeInMillis >= (1 + 2 + 4) * 1000
    }

    @Test public void endToEnd_authorizedRightAway() {
        final def port = wireMockRule.port();
        final def deviceEndpoint = new URI(PROTOCOL, null, host, port, DEVICE_ENDPOINT_PATH, null, null);
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        stubDeviceEndpoint();
        stubTokenEndpointSuccess();
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

    @Test public void endToEnd_throughProxyServer() {
        final def port = wireMockRule.port();
        final def oldProperties = System.properties;
        final def adapter = new LoggingFiltersSourceAdapter();
        final def deviceEndpoint = new URI(PROTOCOL, null, host, port, DEVICE_ENDPOINT_PATH, null, null);
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        stubDeviceEndpoint();
        stubTokenEndpointSuccess();
        final HttpProxyServer proxyServer =
                DefaultHttpProxyServer
                        .bootstrap()
                        .withAddress(PROXY_LISTEN_ADDRESS)
                        .withFiltersSource(adapter)
                        .start();
        final def cut = new DeviceFlowImpl();

        try {
            final def tempProperties = new Properties(oldProperties);
            tempProperties.setProperty("http.proxyHost", host);
            tempProperties.setProperty("http.nonProxyHosts", "");
            final InetSocketAddress proxyAddress = proxyServer.getListenAddress();
            tempProperties.setProperty("http.proxyPort", Integer.toString(proxyAddress.getPort(), 10));
            System.properties = tempProperties;

            final def actualResponse = cut.requestAuthorization(deviceEndpoint, CLIENT_ID, null);

            assert DEVICE_CODE == actualResponse.deviceCode;
            assert USER_CODE == actualResponse.userCode;
            assert VERIFICATION_URI == actualResponse.verificationUri;
            assert EXPIRY_SECONDS == actualResponse.expiresIn;
            assert ATTEMPT_INTERVAL == actualResponse.interval;
            Assert.assertTrue(adapter.proxyWasUsed());

            adapter.reset();

            final def actualTokenPair = cut.requestToken(tokenEndpoint, CLIENT_ID, actualResponse);

            final def actualAccessToken = actualTokenPair.AccessToken;
            assert TokenType.Access == actualAccessToken.Type;
            assert ACCESS_TOKEN == actualAccessToken.Value;
            Assert.assertTrue(adapter.proxyWasUsed());
        }
        finally {
            System.properties = oldProperties;
            proxyServer.stop();
        }
    }

    @Test public void endToEnd_deniedRightAway() {
        final def port = wireMockRule.port();
        final def deviceEndpoint = new URI(PROTOCOL, null, host, port, DEVICE_ENDPOINT_PATH, null, null);
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        stubDeviceEndpoint();
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "access_denied");
        final def cut = new DeviceFlowImpl();

        final def actualResponse = cut.requestAuthorization(deviceEndpoint, CLIENT_ID, null);

        assert DEVICE_CODE == actualResponse.deviceCode;
        assert USER_CODE == actualResponse.userCode;
        assert VERIFICATION_URI == actualResponse.verificationUri;
        assert EXPIRY_SECONDS == actualResponse.expiresIn;
        assert ATTEMPT_INTERVAL == actualResponse.interval;

        try {
            cut.requestToken(tokenEndpoint, CLIENT_ID, actualResponse)
        }
        catch (final AuthorizationException e) {
            assert "access_denied" == e.code;
            return;
        }
        Assert.fail("An AuthorizationException should have been thrown");
    }

    @Test public void endToEnd_authorizedAfterOnePending() {
        final def port = wireMockRule.port();
        final def deviceEndpoint = new URI(PROTOCOL, null, host, port, DEVICE_ENDPOINT_PATH, null, null);
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        stubDeviceEndpoint();
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "authorization_pending");
        stubTokenEndpointSuccess();
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

    @Test public void endToEnd_deniedAfterOnePending() {
        final def port = wireMockRule.port();
        final def deviceEndpoint = new URI(PROTOCOL, null, host, port, DEVICE_ENDPOINT_PATH, null, null);
        final def tokenEndpoint = new URI(PROTOCOL, null, host, port, TOKEN_ENDPOINT_PATH, null, null);
        stubDeviceEndpoint();
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "authorization_pending");
        stubTokenEndpointError("grant_type=device_code&code=${DEVICE_CODE}&client_id=${CLIENT_ID}", "access_denied");
        final def cut = new DeviceFlowImpl();

        final def actualResponse = cut.requestAuthorization(deviceEndpoint, CLIENT_ID, null);

        assert DEVICE_CODE == actualResponse.deviceCode;
        assert USER_CODE == actualResponse.userCode;
        assert VERIFICATION_URI == actualResponse.verificationUri;
        assert EXPIRY_SECONDS == actualResponse.expiresIn;
        assert ATTEMPT_INTERVAL == actualResponse.interval;

        try {
            cut.requestToken(tokenEndpoint, CLIENT_ID, actualResponse)
        }
        catch (final AuthorizationException e) {
            assert "access_denied" == e.code;
            return;
        }
        Assert.fail("An AuthorizationException should have been thrown");
    }
}
