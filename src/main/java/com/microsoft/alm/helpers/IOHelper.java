// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class IOHelper
{
    public static void closeQuietly(final Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (final IOException ignored)
            {
            }
        }
    }

    public static String readFileToString(final File file) throws IOException
    {
        FileInputStream fis = null;
        try
        {
            fis = new FileInputStream(file);
            return readToString(fis);
        }
        finally
        {
            IOHelper.closeQuietly(fis);
        }
    }

    public static String readToString(final InputStream stream) throws IOException
    {
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try
        {
            isr = new InputStreamReader(stream);
            reader = new BufferedReader(isr);

            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
                sb.append(Environment.NewLine);
            }
            return sb.toString();
        }
        finally
        {
            IOHelper.closeQuietly(reader);
            IOHelper.closeQuietly(isr);
        }
    }
}
