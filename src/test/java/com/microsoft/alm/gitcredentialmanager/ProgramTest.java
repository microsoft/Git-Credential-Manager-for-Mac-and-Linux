// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

import com.microsoft.alm.helpers.Trace;
import com.microsoft.alm.oauth2.useragent.Provider;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

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

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.10.6").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.11.0").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "11.1.1").size());
    }

    @Test public void checkOsRequirements_macOsBadVersion()
    {
        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.10.1").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.10.4").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.9.10").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.9.5").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.9.1").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "9.10.5").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "9.11.5").size());
    }

    @Test public void checkOsRequirements_linuxOsHappy()
    {
        Assert.assertEquals(0, Program.checkOsRequirements("Linux", "1.1.1").size());
    }

    @Test public void checkOsRequirements_unsupportedOs()
    {
        Assert.assertEquals(1, Program.checkOsRequirements("Windows", "1.1.1").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Unknown", "1.1.1").size());
    }

    private static final Provider TestProviderFoo = new Provider("foo")
    {
        @Override public List<String> checkRequirements()
        {
            return null;
        }

        @Override public void augmentProcessParameters(List<String> list, List<String> list1)
        {

        }
    };
    private static final Provider TestProviderBar = new Provider("bar")
    {
        @Override public List<String> checkRequirements()
        {
            return null;
        }

        @Override public void augmentProcessParameters(List<String> list, List<String> list1)
        {

        }
    };
    private static final Provider TestProviderPateChinois = new Provider("Pâté Chinois")
    {
        @Override public List<String> checkRequirements()
        {
            return Arrays.asList("steak", "blé d'inde", "patates");
        }

        @Override public void augmentProcessParameters(List<String> list, List<String> list1)
        {

        }
    };
    private static final Provider TestProviderBananaNutChocolateCake = new Provider("Banana Nut Chocolate Cake")
    {
        @Override public List<String> checkRequirements()
        {
            return Arrays.asList("bananas", "nuts", "chocolate");
        }

        @Override public void augmentProcessParameters(List<String> list, List<String> list1)
        {

        }
    };

    @Test public void checkUserAgentProviderRequirements_allFine() throws Exception
    {
        final List<Provider> input = Arrays.asList(TestProviderFoo, TestProviderBar);

        final List<String> actual = Program.checkUserAgentProviderRequirements(input);

        Assert.assertEquals(0, actual.size());
    }

    @Test public void checkUserAgentProviderRequirements_allWrong() throws Exception
    {
        final List<Provider> input = Arrays.asList(TestProviderPateChinois, TestProviderBananaNutChocolateCake);

        final List<String> actual = Program.checkUserAgentProviderRequirements(input);

        Assert.assertEquals("The Pâté Chinois user agent provider has the following unmet requirements:", actual.get(0));
        Assert.assertEquals(" - steak", actual.get(1));
        Assert.assertEquals(" - blé d'inde", actual.get(2));
        Assert.assertEquals(" - patates", actual.get(3));
        Assert.assertEquals("The Banana Nut Chocolate Cake user agent provider has the following unmet requirements:", actual.get(4));
        Assert.assertEquals(" - bananas", actual.get(5));
        Assert.assertEquals(" - nuts", actual.get(6));
        Assert.assertEquals(" - chocolate", actual.get(7));
        Assert.assertEquals(8, actual.size());
    }

    @Test public void checkUserAgentProviderRequirements_oneFineOneWrong() throws Exception
    {
        final List<Provider> input = Arrays.asList(TestProviderFoo, TestProviderPateChinois);

        final List<String> actual = Program.checkUserAgentProviderRequirements(input);

        Assert.assertEquals(0, actual.size());
    }

    @Test public void determinePathToJar_typical() throws Exception
    {
        final URL jarUrl = URI.create("file:/home/example/git-credential-manager/git-credential-manager-1.0.0.jar!/com/microsoft/alm/gitcredentialmanager/").toURL();

        final String actual = Program.determinePathToJar(jarUrl);

        final String expected = "/home/example/git-credential-manager/git-credential-manager-1.0.0.jar";
        Assert.assertEquals(expected, actual);
    }

    @Test public void determinePathToJar_withSpaces() throws Exception
    {
        final URL jarUrl = URI.create("file:/home/example/with%20spaces/git-credential-manager-1.0.0.jar!/com/microsoft/alm/gitcredentialmanager/").toURL();

        final String actual = Program.determinePathToJar(jarUrl);

        final String expected = "/home/example/with spaces/git-credential-manager-1.0.0.jar";
        Assert.assertEquals(expected, actual);
    }
}
