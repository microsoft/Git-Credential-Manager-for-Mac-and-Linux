// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Inspired by System.Net.Http.StringContent.
 */
public class StringContent
{
    private static final String UTF8 = Charset.forName("UTF-8").name();
    private static final String CONTENT_TYPE_TEMPLATE = "%1$s; charset=%2$s";

    public final Map<String, String> Headers = new LinkedHashMap<String, String>();

    private final byte[] bytes;

    private StringContent(final String content, final String mediaType)
    {
        bytes = StringHelper.UTF8GetBytes(content);
        final String contentType = String.format(CONTENT_TYPE_TEMPLATE, mediaType, UTF8);
        Headers.put("Content-Type", contentType);
        final String contentLength = Integer.toString(bytes.length, 10);
        Headers.put("Content-Length", contentLength);
    }

    public static StringContent createUrlEncoded(final QueryString parameters)
    {
        return new StringContent(parameters.toString(), "application/x-www-form-urlencoded");
    }

    public void write(final HttpURLConnection connection) throws IOException
    {
        for (final Map.Entry<String, String> entry : Headers.entrySet())
        {
            final String key = entry.getKey();
            final String value = entry.getValue();
            connection.setRequestProperty(key, value);
        }
        OutputStream outputStream = null;
        try
        {
            outputStream = connection.getOutputStream();
            outputStream.write(bytes);
        }
        finally
        {
            IOHelper.closeQuietly(outputStream);
        }
    }
}
