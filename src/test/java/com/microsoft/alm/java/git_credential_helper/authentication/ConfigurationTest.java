package com.microsoft.alm.java.git_credential_helper.authentication;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

public class ConfigurationTest
{
    @Test
    public void parseGitConfig_simple() throws Exception
    {
        final String input = "\n" +
                "[core]\n" +
                "    autocrlf = false\n" +
                "";

        final Map<String, String> values = testParseGitConfig(input);

        Assert.assertEquals("false", values.get("core.autocrlf"));
    }

    @Test
    public void parseGitConfig_overwritesValues() throws Exception
    {
        final String input = "\n" +
                "[core]\n" +
                "    autocrlf = true\n" +
                "    autocrlf = FileNotFound\n" +
                "    autocrlf = false\n" +
                "";

        final Map<String, String> values = testParseGitConfig(input);

        Assert.assertEquals("false", values.get("core.autocrlf"));
    }

    @Test
    public void parseGitConfig_partiallyQuoted() throws Exception
    {
        final String input = "\n" +
                "[core \"oneQuote]\n" +
                "    autocrlf = \"false\n" +
                "";

        final Map<String, String> values = testParseGitConfig(input);

        Assert.assertEquals("false", values.get("core.oneQuote.autocrlf"));
    }

    @Test
    public void parseGitConfig_sampleFile() throws Exception
    {
        final TreeMap<String, String> values = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        final Class<? extends ConfigurationTest> me = this.getClass();

        final BufferedReader br = new BufferedReader(new InputStreamReader(me.getResourceAsStream("sample.gitconfig")));
        try
        {
            Configuration.parseGitConfig(br, values);
        }
        finally
        {
            br.close();
        }

        Assert.assertEquals(36, values.size());
        Assert.assertEquals("The quotes remained.", "\\\"C:/Utils/Compare It!/wincmp3.exe\\\" \\\"$(cygpath -w \\\"$LOCAL\\\")\\\" \\\"$(cygpath -w \\\"$REMOTE\\\")\\\"", values.get("difftool.cygcompareit.cmd"));
        Assert.assertEquals("The quotes were stripped.", "!f() { git fetch origin && git checkout -b $1 origin/master --no-track; }; f", values.get("alias.cob"));
    }

    @Test
    public void readThroughPublicMethods() throws IOException, URISyntaxException
    {
        final String input = "\n" +
            "[core]\n" +
            "    autocrlf = false\n" +
            "[credential \"microsoft.visualstudio.com\"]\n" +
            "    authority = AAD\n" +
            "[credential \"visualstudio.com\"]\n" +
            "    authority = MSA\n" +
            "[credential \"https://ntlm.visualstudio.com\"]\n" +
            "    authority = NTLM\n" +
            "[credential]\n" +
            "    helper = manager\n" +
            "";
        Configuration cut;

        final BufferedReader br = new BufferedReader(new StringReader(input));
        try
        {
            cut = new Configuration(br);
        }
        finally
        {
            br.close();
        }

        Assert.assertEquals(true, cut.containsKey("CoRe.AuToCrLf"));
        Assert.assertEquals("false", cut.get("CoRe.AuToCrLf"));

        AtomicReference<Configuration.Entry> entryRef = new AtomicReference<Configuration.Entry>();
        Assert.assertEquals(true, cut.tryGetEntry("core", (String) null, "autocrlf", entryRef));
        Assert.assertEquals("false", entryRef.get().Value);

        Assert.assertEquals(true, cut.tryGetEntry("credential", new URI("https://microsoft.visualstudio.com"), "authority", entryRef));
        Assert.assertEquals("AAD", entryRef.get().Value);

        Assert.assertEquals(true, cut.tryGetEntry("credential", new URI("https://mseng.visualstudio.com"), "authority", entryRef));
        Assert.assertEquals("MSA", entryRef.get().Value);

        Assert.assertEquals(true, cut.tryGetEntry("credential", new URI("https://ntlm.visualstudio.com"), "authority", entryRef));
        Assert.assertEquals("NTLM", entryRef.get().Value);
    }

    private static Map<String, String> testParseGitConfig(final String input) throws IOException
    {
        final TreeMap<String, String> values = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

        final BufferedReader br = new BufferedReader(new StringReader(input));
        try
        {
            Configuration.parseGitConfig(br, values);
        }
        finally
        {
            br.close();
        }
        return values;
    }
}
