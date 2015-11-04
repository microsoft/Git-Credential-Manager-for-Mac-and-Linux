// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

import com.microsoft.alm.helpers.Trace;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

public class ProgramTest
{
    private final ByteArrayOutputStream OUTPUT_STREAM = new ByteArrayOutputStream();
    private Program program;

    @Before
    public void setup()
	{
        OUTPUT_STREAM.reset();
        program = new Program(null, new PrintStream(OUTPUT_STREAM), null);
    }

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
        if (!output.startsWith(expected))
        {
            Assert.fail("'" + output + "' did not start with '" + expected + "'.");
        }
        if (!output.contains("password="))
        {
            Assert.fail("'" + output + "' did not contain 'password='.");
        }
    }

    @Test public void isValidGitVersion_happy()
	{
        // greater version
        Assert.assertTrue(program.isValidGitVersion("git version 2.4.9"));

        // min version
        Assert.assertTrue(program.isValidGitVersion("git version 1.9.0"));
    }

    @Test public void isValidGitVersion_badVersion()
	{
        Assert.assertFalse(program.isValidGitVersion("git version 1.8.3"));
    }

    @Test public void isValidGitVersion_noResponse()
	{
        Assert.assertFalse(program.isValidGitVersion(null));
    }

    @Test public void isValidGitVersion_noGitFound()
	{
        Assert.assertFalse(program.isValidGitVersion("-bash: git: command not found"));
    }

    @Test public void checkOsRequirements_macOsHappy()
	{
        System.setProperty("os.name", "Mac OS X");
        System.setProperty("os.version", "10.10.5");
        Assert.assertTrue(program.checkOsRequirements());
    }

    @Test public void checkOsRequirements_macOsBadVersion()
	{
        System.setProperty("os.name", "Mac OS X");
        System.setProperty("os.version", "10.10.1");
        Assert.assertFalse(program.checkOsRequirements());

        System.setProperty("os.version", "10.9.5");
        Assert.assertFalse(program.checkOsRequirements());

        System.setProperty("os.version", "9.10.5");
        Assert.assertFalse(program.checkOsRequirements());
    }

    @Test public void checkOsRequirements_linuxOsHappy()
	{
        System.setProperty("os.name", "Linux");
        Assert.assertTrue(program.checkOsRequirements());
    }

    @Test public void checkOsRequirements_invalidOs()
	{
        System.setProperty("os.name", "Windows");
        Assert.assertFalse(program.checkOsRequirements());
    }
}
