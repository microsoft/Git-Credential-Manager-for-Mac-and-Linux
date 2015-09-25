// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Debug;
import com.microsoft.alm.helpers.Environment;
import com.microsoft.alm.helpers.Path;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.helpers.Trace;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Configuration
{
    private static final Pattern CommentLinePattern = Pattern.compile
    (
    //   ^\s*[#;]
        "^\\s*[#;]"
    );
    private static final Pattern SectionNamePattern = Pattern.compile
    (
    //   ^\s*\[\s*(\w+)\s*(\"[^\"]+\"){0,1}\]
        "^\\s*\\[\\s*(\\w+)\\s*(\\\"[^\\]]+){0,1}\\]"
    );
    private static final Pattern NameValuePattern = Pattern.compile
    (
    //   ^\s*(\w+)\s*=\s*(.+)
        "^\\s*(\\w+)\\s*=\\s*(.+)"
    );

    private static final char HostSplitCharacter = '.';

    public Configuration(final String directory) throws IOException
    {
        if (StringHelper.isNullOrWhiteSpace(directory))
            throw new IllegalArgumentException("directory is null or empty");
        if (!Path.directoryExists(directory))
            throw new IllegalArgumentException("directory does not exist");

        loadGitConfiguration(directory);
    }

    public Configuration() throws IOException
    {
        this(Environment.getCurrentDirectory());
    }

    Configuration(final BufferedReader configReader) throws IOException
    {
        parseGitConfig(configReader, _values);
    }

    private final Map<String, String> _values = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

    public String get(final String key)
    {
        return _values.get(key);
    }

    public boolean containsKey(final String key)
    {
        return _values.containsKey(key);
    }

    public boolean tryGetEntry(final String prefix, final String key, final String suffix, final AtomicReference<Entry> entry)
    {
        Debug.Assert(prefix != null, "The prefix parameter is null");
        Debug.Assert(suffix != null, "The suffix parameter is null");

        String match = StringHelper.isNullOrEmpty(key)
                ? String.format("%1$s.%2$s", prefix, suffix)
                : String.format("%1$s.%2$s.%3$s", prefix, key, suffix);

        // if there's a match, return it
        if (_values.containsKey(match))
        {
            entry.set(new Entry(match, _values.get(match)));
            return true;
        }

        // nothing found
        entry.set(null);
        return false;
    }

    public boolean tryGetEntry(final String prefix, final URI targetUri, final String key, final AtomicReference<Entry> entry)
    {
        Debug.Assert(key != null, "The key parameter is null");

        Trace.writeLine("Configuration::tryGetEntry");

        if (targetUri != null)
        {
            // return match seeking from most specific (<prefix>.<scheme>://<host>.<key>) to least specific (credential.<key>)
            if (tryGetEntry(prefix, String.format("%1$s://%2$s", targetUri.getScheme(), targetUri.getHost()), key, entry)
                    || tryGetEntry(prefix, targetUri.getHost(), key, entry))
                return true;

            if (!StringHelper.isNullOrWhiteSpace(targetUri.getHost()))
            {
                final String[] fragments = targetUri.getHost().split("\\" + HostSplitCharacter);
                String host = null;

                // look for host matches stripping a single sub-domain at a time off
                // don't match against a top-level domain (aka ".com")
                for (int i = 1; i < fragments.length - 1; i++)
                {
                    host = StringHelper.join(".", fragments, i, fragments.length - i);
                    if (tryGetEntry(prefix, host, key, entry))
                        return true;
                }
            }
        }

        // try to find an unadorned match as a complete fallback
        if (tryGetEntry(prefix, StringHelper.Empty, key, entry))
            return true;

        // nothing found
        entry.set(null);
        return false;
    }

    public void loadGitConfiguration(final String directory) throws IOException
    {
        final AtomicReference<String> systemConfig = new AtomicReference<String>();
        final AtomicReference<String> globalConfig = new AtomicReference<String>();
        final AtomicReference<String> localConfig = new AtomicReference<String>();

        Trace.writeLine("Configuration::loadGitConfiguration");

        // read Git's three configs from lowest priority to highest, overwriting values as
        // higher priority configurations are parsed, storing them in a handy lookup table

        // find and parse Git's system config
        if (Where.gitSystemConfig(systemConfig))
        {
            parseGitConfig(systemConfig.get());
        }

        // find and parse Git's global config
        if (Where.gitGlobalConfig(globalConfig))
        {
            parseGitConfig(globalConfig.get());
        }

        // find and parse Git's local config
        if (Where.gitLocalConfig(directory, localConfig))
        {
            parseGitConfig(localConfig.get());
        }

        for (final Map.Entry pair : _values.entrySet())
        {
            Trace.writeLine(String.format("   %1$s = %2$s", pair.getKey(), pair.getValue()));
        }
    }

    private void parseGitConfig(final String configPath) throws IOException
    {
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(configPath), "The configPath parameter is null or invalid.");
        Debug.Assert(Path.fileExists(configPath), "The configPath parameter references a non-existent file.");
        Debug.Assert(_values != null, "The configPath parameter is null or invalid.");

        Trace.writeLine("Configuration::ParseGitConfig");

        if (!Path.fileExists(configPath))
            return;

        final BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(configPath)));
        try
        {
            parseGitConfig(br, _values);
        }
        finally
        {
            br.close();
        }
    }

    static void parseGitConfig(final BufferedReader configReader, final Map<String, String> destination) throws IOException
    {
        Matcher match = null;
        String section = null;

        // parse each line in the config independently - Git's configs do not accept multi-line values
        String line;
        while ((line = configReader.readLine()) != null)
        {
            // skip empty and commented lines
            if (StringHelper.isNullOrWhiteSpace(line))
                continue;
            if (CommentLinePattern.matcher(line).matches())
                continue;

            // sections begin with values like [section] or [section "section name"]. All subsequent lines,
            // until a new section is encountered, are children of the section
            if ((match = SectionNamePattern.matcher(line)).matches())
            {
                // NOTE: in Java, match.groupCount() is one less than .NET's match.Groups.Count
                if (match.groupCount() >= 1 && !StringHelper.isNullOrWhiteSpace(match.group(1)))
                {
                    section = match.group(1).trim();

                    // check if the section is named, if so: process the name
                    if (match.groupCount() >= 2 && !StringHelper.isNullOrWhiteSpace(match.group(2)))
                    {
                        String val = match.group(2).trim();

                        // triming off enclosing quotes makes usage easier, only trim in pairs
                        if (val.charAt(0) == '"')
                        {
                            // NOTE: Java: substring(beginIndex, endIndex), .NET: Substring(startIndex, length)
                            if (val.charAt(val.length() - 1) == '"')
                            {
                                val = val.substring(1, val.length() - 1);
                            }
                            else
                            {
                                val = val.substring(1, val.length() - 0);
                            }
                        }

                        section += HostSplitCharacter + val;
                    }
                }
            }
            // section children should be in the format of name = value pairs
            else if ((match = NameValuePattern.matcher(line)).matches())
            {
                if (match.groupCount() >= 2
                    && !StringHelper.isNullOrWhiteSpace(match.group(1))
                    && !StringHelper.isNullOrWhiteSpace(match.group(2)))
                {
                    final String key = section + HostSplitCharacter + match.group(1).trim();
                    String val = match.group(2).trim();

                    // triming off enclosing quotes makes usage easier, only trim in pairs
                    if (val.charAt(0) == '"')
                    {
                        // NOTE: Java: substring(beginIndex, endIndex), .NET: Substring(startIndex, length)
                        if (val.charAt(val.length() - 1) == '"')
                        {
                            val = val.substring(1, val.length() - 1);
                        }
                        else
                        {
                            val = val.substring(1, val.length() - 0);
                        }
                    }

                    // add or update the (key, value)
                    destination.put(key, val);
                }
            }
        }
    }

    public class Entry
    {
        public Entry(final String key, final String value)
        {
            Key = key;
            Value = value ;
        }

        public final String Key;
        public final String Value;
    }
}

