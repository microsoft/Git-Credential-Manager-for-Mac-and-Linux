// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * A class to test {@see DeviceFlowResponse}.
 */
@CompileStatic
public class DeviceFlowResponseTest {

    @Test public void fromJson_deviceFlowDraft01() {
        final now = Calendar.instance
        final def input = """\
{
    "device_code":"74tq5miHKB",
    "user_code":"94248",
    "verification_uri":"https://www.example.com/device",
    "interval":5
}
"""

        final actual = DeviceFlowResponse.fromJson(input)

        assert "74tq5miHKB" == actual.deviceCode
        assert "94248" == actual.userCode
        assert URI.create("https://www.example.com/device") == actual.verificationUri
        assert 5 == actual.interval
        assert 600 == actual.expiresIn
        assert actual.expiresAt.timeInMillis - now.timeInMillis >= 600
    }

}
