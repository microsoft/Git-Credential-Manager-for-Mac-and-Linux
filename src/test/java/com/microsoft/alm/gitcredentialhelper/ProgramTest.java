// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialhelper;

import com.microsoft.alm.authentication.Configuration;
import com.microsoft.alm.authentication.IAuthentication;
import com.microsoft.alm.authentication.ISecureStore;
import com.microsoft.alm.helpers.Environment;
import com.microsoft.alm.helpers.Trace;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
        final Program program = new Program(inputStream, new PrintStream(outputStream), new IComponentFactory()
        {
            @Override
            public IAuthentication createAuthentication(final OperationArguments operationArguments, final ISecureStore secureStore)
            {
                return Program.createAuthentication(operationArguments, secureStore);
            }

            @Override
            public Configuration createConfiguration() throws IOException
            {
                return new Configuration();
            }

            @Override
            public ISecureStore createSecureStore()
            {
                final String home = Environment.getFolderPath(Environment.SpecialFolder.UserProfile);
                final File insecureFile = new File(home, "insecureStore.xml");
                return new InsecureStore(insecureFile);
            }
        });

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
