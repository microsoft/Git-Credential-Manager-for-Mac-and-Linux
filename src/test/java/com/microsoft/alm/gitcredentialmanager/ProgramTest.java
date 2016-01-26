// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialmanager;

import com.microsoft.alm.helpers.Func;
import com.microsoft.alm.helpers.Trace;
import com.microsoft.alm.oauth2.useragent.Provider;
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcess;
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcessFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
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

    @Test public void checkGitRequirements() throws UnsupportedEncodingException
    {
        final TestableProcess process = new TestProcess("git version 2.6.2\n");
        final TestableProcessFactory processFactory = new TestableProcessFactory()
        {
            @Override public TestableProcess create(final String... strings) throws IOException
            {
                return process;
            }
        };

        final List<String> actual = Program.checkGitRequirements(processFactory);

        Assert.assertEquals(0, actual.size());
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
        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.9.5").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.9.6").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.10.1").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.10.4").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.10.5").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.10.6").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "10.11.0").size());

        Assert.assertEquals(0, Program.checkOsRequirements("Mac OS X", "11.1.1").size());
    }

    @Test public void checkOsRequirements_macOsBadVersion()
    {
        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.9.1").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.9.4").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.8.10").size());

        Assert.assertEquals(1, Program.checkOsRequirements("Mac OS X", "10.8.5").size());

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

    @Test public void configureGit_perUserWithOpenJdkOnFedoraLinux() throws Exception
    {
        final TestableProcess process = new TestProcess("");
        final TestableProcessFactory processFactory = new TestableProcessFactory()
        {
            @Override
            public TestableProcess create(final String... strings) throws IOException
            {
                Assert.assertEquals("git", strings[0]);
                Assert.assertEquals("config", strings[1]);
                Assert.assertEquals("--global", strings[2]);
                Assert.assertEquals("--add", strings[3]);
                Assert.assertEquals("credential.helper", strings[4]);
                Assert.assertEquals("!/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.65-3.b17.fc22.x86_64/bin/java -Ddebug=false -jar /usr/bin/git-credential-manager-1.1.0.jar", strings[5]);
                return process;
            }
        };
        Program.configureGit(processFactory, "global", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.65-3.b17.fc22.x86_64/bin/java", "/usr/bin/git-credential-manager-1.1.0.jar", false);
    }

    @Test public void configureGit_allUsersDebugWithOracleJdkOnMac() throws Exception
    {
        final TestableProcess process = new TestProcess("");
        final TestableProcessFactory processFactory = new TestableProcessFactory()
        {
            @Override
            public TestableProcess create(final String... strings) throws IOException
            {
                Assert.assertEquals("git", strings[0]);
                Assert.assertEquals("config", strings[1]);
                Assert.assertEquals("--system", strings[2]);
                Assert.assertEquals("--add", strings[3]);
                Assert.assertEquals("credential.helper", strings[4]);
                Assert.assertEquals("!/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java -Ddebug=true -jar /usr/local/bin/git-credential-manager-1.1.0.jar", strings[5]);
                return process;
            }
        };
        Program.configureGit(processFactory, "system", "/System/Library/Frameworks/JavaVM.framework/Versions/Current/Commands/java", "/usr/local/bin/git-credential-manager-1.1.0.jar", true);
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

    private static class FakeFileChecker implements Func<File, Boolean>
    {
        private final String expectedPath;
        public FakeFileChecker(final String expectedPath)
        {
            this.expectedPath = expectedPath;
        }
        @Override public Boolean call(File file)
        {
            final String absolutePath = file.getAbsolutePath();
            final boolean result = absolutePath.equals(expectedPath);
            return result;
        }
    }

    @Test public void findProgram_notFound() throws Exception
    {
        final List<String> directories = Arrays.asList("/usr/bin", "/usr/local/bin", "/bin");
        final File expectedFile = new File("/usr/sbin/git-credential-osxkeychain");
        final String expectedPath = expectedFile.getAbsolutePath();
        final String executableName = "git-credential-osxkeychain";
        final Func<File, Boolean> isFile = new FakeFileChecker(expectedPath);

        final File actual = Program.findProgram(directories, executableName, isFile);

        Assert.assertEquals(null, actual);
    }

    @Test public void findProgram_found() throws Exception
    {
        final List<String> directories = Arrays.asList("/usr/bin", "/usr/local/bin", "/bin");
        final File expectedFile = new File("/usr/local/bin/git-credential-osxkeychain");
        final String expectedPath = expectedFile.getAbsolutePath();
        final String executableName = "git-credential-osxkeychain";
        final Func<File, Boolean> isFile = new FakeFileChecker(expectedPath);

        final File actual = Program.findProgram(directories, executableName, isFile);

        Assert.assertNotNull(actual);
        Assert.assertEquals(expectedPath, actual.getAbsolutePath());
    }

    @Test public void findProgram_fromPathString_notFound() throws Exception
    {
        final String pathString = "/usr/bin:/usr/local/bin:/bin";
        final File expectedFile = new File("/usr/sbin/git-credential-osxkeychain");
        final String expectedPath = expectedFile.getAbsolutePath();
        final String executableName = "git-credential-osxkeychain";
        final Func<File, Boolean> isFile = new FakeFileChecker(expectedPath);

        final File actual = Program.findProgram(pathString, ":", executableName, isFile);

        Assert.assertEquals(null, actual);
    }

    @Test public void findProgram_fromPathString_found() throws Exception
    {
        final String pathString = "/usr/bin:/usr/local/bin:/bin";
        final File expectedFile = new File("/usr/local/bin/git-credential-osxkeychain");
        final String expectedPath = expectedFile.getAbsolutePath();
        final String executableName = "git-credential-osxkeychain";
        final Func<File, Boolean> isFile = new FakeFileChecker(expectedPath);

        final File actual = Program.findProgram(pathString, ":", executableName, isFile);

        Assert.assertNotNull(actual);
        Assert.assertEquals(expectedPath, actual.getAbsolutePath());
    }
}
