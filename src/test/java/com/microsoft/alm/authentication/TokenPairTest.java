// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import org.junit.Assert;
import org.junit.Test;

public class TokenPairTest
{

    @Test public void accessTokenResponse_RFC6749()
    {
        final String input =
            "     {\n" +
            "       \"access_token\":\"2YotnFZFEjr1zCsicMWpAA\",\n" +
            "       \"token_type\":\"example\",\n" +
            "       \"expires_in\":3600,\n" +
            "       \"refresh_token\":\"tGzv3JOkF0XG5Qx2TlKWIA\",\n" +
            "       \"example_parameter\":\"example_value\"\n" +
            "     }";

        final TokenPair actual = new TokenPair(input);

        Assert.assertEquals("2YotnFZFEjr1zCsicMWpAA", actual.AccessToken.Value);
        Assert.assertEquals("tGzv3JOkF0XG5Qx2TlKWIA", actual.RefreshToken.Value);
        Assert.assertEquals("3600.0", actual.Parameters.get("expires_in"));
        Assert.assertEquals("example_value", actual.Parameters.get("example_parameter"));
        Assert.assertEquals("example", actual.Parameters.get("token_type"));
    }

}
