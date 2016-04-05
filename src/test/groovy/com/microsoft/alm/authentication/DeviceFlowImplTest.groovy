// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication

import com.github.tomakehurst.wiremock.junit.WireMockRule
import groovy.transform.CompileStatic
import org.junit.Rule
import org.junit.Test

/**
 * A class to test {@see DeviceFlowImpl}.
 */
@CompileStatic
public class DeviceFlowImplTest {

    private static final String PROTOCOL = "http";
    private static final String DEVICE_CODE = "9297fb18-46d0-4846-97ca-ab8dd3b55729";
    private static final String USER_CODE = "A1B2B4C1C5D1D3E3E5";
    private static final String ACCESS_TOKEN = "d15281b1-03f1-4581-90d3-4527d9cf4147";
    private static final URI VERIFICATION_URI = new URI("http://verification.example.com");
    private static final int EXPIRY_SECONDS = 600;
    private static final int ATTEMPT_INTERVAL = 1;
    private static final DeviceFlowResponse DEFAULT_DEVICE_FLOW_RESPONSE = new DeviceFlowResponse(DEVICE_CODE, USER_CODE, VERIFICATION_URI, EXPIRY_SECONDS, ATTEMPT_INTERVAL);

    private final String host;

    @Rule public WireMockRule wireMockRule = new WireMockRule(0);

    public DeviceFlowImplTest() {
        final def localHostAddress = InetAddress.localHost;
        host = localHostAddress.hostName;
    }

}
