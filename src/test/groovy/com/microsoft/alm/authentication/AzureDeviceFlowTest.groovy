// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * A class to test {@link AzureDeviceFlow}.
 */
@CompileStatic
public class AzureDeviceFlowTest {

    @Test public void buildTokenPair_sampleResponse() {
        final input = /{"token_type":"Bearer","scope":"user_impersonation","expires_in":"3600","expires_on":"1460690464","not_before":"1460686564","resource":"4e725760-015b-4168-8adf-e8329e863974","access_token":"Q29uZ3JhdHVsYXRpb25zLCB5b3UgaGF2ZSBzdWNjZXNzZnVsbHkgZGVjb2RlZCBhIGZha2UgYWNjZXNzIHRva2VuLg==","refresh_token":"Tm93IHlvdSBzdWNjZXNzZnVsbHkgZGVjb2RlZCBhIGZha2UgcmVmcmVzaCB0b2tlbi4=","id_token":"SSBub3RpY2UgeW91J3JlIHN0aWxsIGRlY29kaW5nIGZha2UgdG9rZW5zLiAgVGhpcyBvbmUncyBmb3IgaWRfdG9rZW4u"}/;
        final cut = new AzureDeviceFlow();

        final actualTokenPair = cut.buildTokenPair(input);

        assert "Q29uZ3JhdHVsYXRpb25zLCB5b3UgaGF2ZSBzdWNjZXNzZnVsbHkgZGVjb2RlZCBhIGZha2UgYWNjZXNzIHRva2VuLg==" == actualTokenPair.AccessToken.Value;
    }

}
