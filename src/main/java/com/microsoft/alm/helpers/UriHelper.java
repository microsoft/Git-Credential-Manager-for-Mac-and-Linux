// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class UriHelper
{
    public static final String UTF_8 = "UTF-8";

    public static boolean isWellFormedUriString(final String uriString)
    {
        try
        {
            new URI(uriString);
            return true;
        }
        catch (final URISyntaxException ignored)
        {
            return false;
        }
    }

    public static QueryString deserializeParameters(final String s)
    {
        final QueryString result = new QueryString();
        // TODO: implement
        return result;
    }

    public static String serializeParameters(final Map<String, String> parameters)
    {
        try {
            final StringBuilder sb = new StringBuilder();
            final Iterator<Map.Entry<String, String>> iterator = parameters.entrySet().iterator();
            if (iterator.hasNext())
            {
                Map.Entry<String, String> entry;
                String key;
                String encodedKey;
                String value;
                String encodedValue;

                entry = iterator.next();
                key = entry.getKey();
                encodedKey = URLEncoder.encode(key, UTF_8);
                sb.append(encodedKey);
                value = entry.getValue();
                if (value != null)
                {
                    encodedValue = URLEncoder.encode(value, UTF_8);
                    sb.append('=').append(encodedValue);
                }
                while (iterator.hasNext())
                {
                    sb.append('&');
                    entry = iterator.next();
                    key = entry.getKey();
                    encodedKey = URLEncoder.encode(key, UTF_8);
                    sb.append(encodedKey);
                    value = entry.getValue();
                    if (value != null)
                    {
                        encodedValue = URLEncoder.encode(value, UTF_8);
                        sb.append('=').append(encodedValue);
                    }
                }
            }
            return sb.toString();
        }
        catch (final UnsupportedEncodingException e) {
            throw new Error(e);
        }

    }
}
