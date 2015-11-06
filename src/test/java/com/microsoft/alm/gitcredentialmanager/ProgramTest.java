// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

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
        Assert.assertEquals(0, Program.isValidGitVersion("git version 2.4.9").size());

        // min version
        Assert.assertEquals(0, Program.isValidGitVersion("git version 1.9.0").size());
    }

    @Test public void isValidGitVersion_badVersion()
    {
        Assert.assertEquals(1, Program.isValidGitVersion("git version 1.8.3").size());
    }

    @Test public void isValidGitVersion_noResponse()
    {
        Assert.assertEquals(1, Program.isValidGitVersion(null).size());
    }

    @Test public void isValidGitVersion_noGitFound()
    {
        Assert.assertEquals(1, Program.isValidGitVersion("-bash: git: command not found").size());
    }

    @Test public void checkOsRequirements_macOsHappy()
    {
        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.10.5").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "11.1.1").size());
    }

    @Test public void checkOsRequirements_macOsBadVersion()
    {
        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.10.1").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.9.5").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "9.10.5").size());
    }

    @Test public void checkOsRequirements_linuxOsHappy()
    {
        Assert.assertEquals(0, Program.checkOsRequirements("Linux", "1.1.1").size());
    }

    @Test public void checkOsRequirements_invalidOs()
    {
        Assert.assertEquals(1, Program.checkOsRequirements("Windows", "1.1.1").size());
    }
}
