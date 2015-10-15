// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpClient
{
    public final Map<String, String> Headers = new LinkedHashMap<String, String>();

    public HttpClient(final String userAgent)
    {
        Headers.put("User-Agent", userAgent);
    }

    public void ensureOK(final HttpURLConnection connection) throws IOException
    {
        final int statusCode = connection.getResponseCode();
        if (statusCode != HttpURLConnection.HTTP_OK)
        {
            InputStream errorStream = null;
            try
            {
                errorStream = connection.getErrorStream();
                final String content = IOHelper.readToString(errorStream);
                final String template = "HTTP request failed with code %1$d: %2$s";
                final String message = String.format(template, statusCode, content);
                throw new IOException(message);
            }
            finally
            {
                IOHelper.closeQuietly(errorStream);
            }
        }
    }

    public static String readToString(final HttpURLConnection connection) throws IOException
    {
        String responseContent;
        InputStream responseStream = null;
        try
        {
            responseStream = connection.getInputStream();
            responseContent = IOHelper.readToString(responseStream);
        }
        finally
        {
            IOHelper.closeQuietly(responseStream);
        }
        return responseContent;
    }

    HttpURLConnection createConnection(final URI uri, final String method, final Action<HttpURLConnection> interceptor)
    {
        final URL url;
        try
        {
            url = uri.toURL();
        }
        catch (final MalformedURLException e)
        {
            throw new Error(e);
        }

        final HttpURLConnection connection;
        try
        {
            connection = (HttpURLConnection) url.openConnection();
        }
        catch (final IOException e)
        {
            throw new Error(e);
        }

        try
        {
            connection.setRequestMethod(method);
        }
        catch (final ProtocolException e)
        {
            throw new Error(e);
        }

        for (final Map.Entry<String, String> entry : Headers.entrySet())
        {
            final String key = entry.getKey();
            final String value = entry.getValue();
            connection.setRequestProperty(key, value);
        }

        if (interceptor != null)
        {
            interceptor.call(connection);
        }

        return connection;
    }

    public HttpURLConnection head(final URI uri) throws IOException
    {
        return head(uri, null);
    }

    public HttpURLConnection head(final URI uri, final Action<HttpURLConnection> interceptor) throws IOException
    {
        final HttpURLConnection connection = createConnection(uri, "HEAD", interceptor);
        connection.connect();

        return connection;
    }

    public HttpURLConnection get(final URI uri) throws IOException
    {
        return get(uri, null);
    }

    public HttpURLConnection get(final URI uri, final Action<HttpURLConnection> interceptor) throws IOException
    {
        final HttpURLConnection connection = createConnection(uri, "GET", interceptor);
        connection.setDoInput(true);

        return connection;
    }

    public HttpURLConnection post(final URI uri, final StringContent content) throws IOException
    {
        return post(uri, content, null);
    }

    public HttpURLConnection post(final URI uri, final StringContent content, final Action<HttpURLConnection> interceptor) throws IOException
    {
        final HttpURLConnection connection = createConnection(uri, "POST", interceptor);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        content.write(connection);

        return connection;
    }
}
