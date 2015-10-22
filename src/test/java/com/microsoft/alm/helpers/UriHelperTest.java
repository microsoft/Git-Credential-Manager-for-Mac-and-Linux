// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class UriHelperTest
{

    @Test
    public void serializeParameters_firstHasNameOnly() throws Exception
    {
        final Map<String, String> input = new LinkedHashMap<String, String>();
        input.put("nameOnly", null);

        final String actual = UriHelper.serializeParameters(input);

        Assert.assertEquals("nameOnly", actual);
    }

    @Test
    public void serializeParameters_secondHasNameOnly() throws Exception
    {
        final Map<String, String> input = new LinkedHashMap<String, String>();
        input.put("name", "value");
        input.put("nameOnly", null);

        final String actual = UriHelper.serializeParameters(input);

        Assert.assertEquals("name=value&nameOnly", actual);
    }

    @Test
    public void serializeParameters_typical() throws Exception
    {
        final Map<String, String> input = new LinkedHashMap<String, String>();
        input.put("resource", "a8860e8f-ca7d-4efe-b80d-4affab13d4ba");
        input.put("client_id", "f7e11bcd-b50b-4869-ad88-8bdd6cbc8473");
        input.put("response_type", "code");
        input.put("redirect_uri", "https://example.com");
        input.put("client-request-id", "06ac412b-8cc0-4ca5-b943-d9dc218abee6");
        input.put("prompt", "login");

        final String actual = UriHelper.serializeParameters(input);

        Assert.assertEquals("resource=a8860e8f-ca7d-4efe-b80d-4affab13d4ba&client_id=f7e11bcd-b50b-4869-ad88-8bdd6cbc8473&response_type=code&redirect_uri=https%3A%2F%2Fexample.com&client-request-id=06ac412b-8cc0-4ca5-b943-d9dc218abee6&prompt=login", actual);
    }

}
