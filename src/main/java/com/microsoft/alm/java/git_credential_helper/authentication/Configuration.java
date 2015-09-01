package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class Configuration
{
    private final char HostSplitCharacter = '.';

    public Configuration(final String directory)
    {
        throw new NotImplementedException();
    }

    public Configuration()
    {
        this("TODO: Environment.CurrentDirectory");
    }

    private final Map<String, String> _values;

    public String get(final String key)
    {
        throw new NotImplementedException();
    }

    public boolean containsKey(final String key)
    {
        throw new NotImplementedException();
    }

    public boolean tryGetEntry(final String prefix, final String key, final String suffix, final AtomicReference<Entry> entry)
    {
        throw new NotImplementedException();
    }

    public boolean tryGetEntry(final String prefix, final URI targetUri, final String key, final AtomicReference<Entry> entry)
    {
        throw new NotImplementedException();
    }

    public void loadGitConfiguration(final String directory)
    {
        throw new NotImplementedException();
    }

    private void parseGitConfig(final String configPath)
    {
        throw new NotImplementedException();
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

