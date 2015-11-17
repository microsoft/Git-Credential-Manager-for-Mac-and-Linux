// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class TestProcess implements TestableProcess
{

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private final ByteArrayInputStream errorStream;
    private final ByteArrayInputStream inputStream;
    private final ByteArrayOutputStream outputStream;

    public TestProcess(final String input)
    {
        this(input, "");
    }

    public TestProcess(final String input, final String error)
    {
        inputStream = new ByteArrayInputStream(input.getBytes(UTF_8));
        errorStream = new ByteArrayInputStream(error.getBytes(UTF_8));
        outputStream = new ByteArrayOutputStream();
    }

    @Override
    public InputStream getErrorStream()
    {
        return errorStream;
    }

    @Override
    public InputStream getInputStream()
    {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public String getOutput() throws UnsupportedEncodingException
    {
        return outputStream.toString(UTF_8.name());
    }

    @Override
    public int waitFor() throws InterruptedException
    {
        return 0;
    }
}
