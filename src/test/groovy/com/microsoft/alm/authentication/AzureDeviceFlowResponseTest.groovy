// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * A class to test {@link AzureDeviceFlowResponse}.
 */
@CompileStatic
public class AzureDeviceFlowResponseTest {

    @Test public void fromJson_azureActiveDirectory() {
        final now = Calendar.instance
        final def input = """\
{"user_code":"EZ2KYPAW4","device_code":"EAAABAAEAiL9Kn2Z27Uubv","verification_url":"https://aka.ms/devicelogin","expires_in":"900","interval":"5","message":"To sign in, use a web browser to open the page https://aka.ms/devicelogin. Enter the code EZ2KYPAW4 to authenticate."}\
"""

        final actual = AzureDeviceFlowResponse.fromJson(input)

        assert "EAAABAAEAiL9Kn2Z27Uubv" == actual.deviceCode
        assert "EZ2KYPAW4" == actual.userCode
        assert URI.create("https://aka.ms/devicelogin") == actual.verificationUri
        assert 5 == actual.interval
        assert 900 == actual.expiresIn
        assert actual.expiresAt.timeInMillis - now.timeInMillis >= 900
        assert "To sign in, use a web browser to open the page https://aka.ms/devicelogin. Enter the code EZ2KYPAW4 to authenticate." == actual.message;
    }
}
