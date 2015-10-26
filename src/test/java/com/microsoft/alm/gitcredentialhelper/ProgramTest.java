// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialhelper;

import com.microsoft.alm.helpers.Trace;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class ProgramTest
{

    @Ignore("This test requires user intervention and must be run manually.")
    @Test public void get() throws Exception
    {
        Trace.getListeners().add(System.err);
        final String hostAccount = System.getProperty("hostAccount");
        final String input = "protocol=https\n" +
            "host=" + hostAccount + ".visualstudio.com\n" +
            "path=\n" +
            "";
        final InputStream inputStream = new ByteArrayInputStream(input.getBytes("UTF-8"));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final Program program = new Program(inputStream, new PrintStream(outputStream), new Program.ComponentFactory());

        program.innerMain(new String[]{"get"});

        final String output = outputStream.toString("UTF-8");
        final String expected = "protocol=https\n" +
            "host=" + hostAccount + ".visualstudio.com\n" +
            "path=\n" +
            "username=Personal Access Token\n";
        Assert.assertTrue(output.startsWith(expected));
        Assert.assertTrue(output.contains("password="));
    }

}
