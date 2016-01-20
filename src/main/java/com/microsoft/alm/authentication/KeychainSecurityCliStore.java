// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.oauth2.useragent.subprocess.DefaultProcessFactory;
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcessFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeychainSecurityCliStore implements ISecureStore
{
    static final String PREFIX = "gcm4ml:";

    private final TestableProcessFactory processFactory;

    public KeychainSecurityCliStore()
    {
        this(new DefaultProcessFactory());
    }

    KeychainSecurityCliStore(final TestableProcessFactory processFactory)
    {
        this.processFactory = processFactory;
    }

    /**
     * Adds a prefix to the target name to avoid a collision
     * with the built-in git-credential-osxkeychain.
     * This is because the built-in helper will not validate the credentials first,
     * leading to a poor user experience if the token is no longer valid.
     *
     * @param targetName the string provided to {@see ISecureStore} methods
     * @return a string suitable for use as the "service name"
     */
    static String createServiceName(final String targetName)
    {
        return PREFIX + targetName;
    }

    private static final Pattern MetadataLinePattern = Pattern.compile
    (
    //   ^(\w+):\s"(.+)"
        "^(\\w+):\\s\"(.+)\""
    );

    static void parseMetadataLine(final String line, final Map<String, Object> destination)
    {
        final Matcher matcher = MetadataLinePattern.matcher(line);
        if (matcher.matches())
        {
            final String key = matcher.group(1);
            final String value = matcher.group(2);
            destination.put(key, value);
        }
    }

    enum AttributeParsingState
    {
        Spaces,
        StringKey,
        HexKey,
        BeforeType,
        Type,
        AfterType,
        BeforeValue,
        NullValue,
        StringValue,
        TimeDateValue,
        ValueFinished,
        ;
    }

    static void parseAttributeLine(final String line, final Map<String, Object> destination)
    {
        final String template = "Undefined transition '%1$s' from %2$s.";
        final StringBuilder key = new StringBuilder();
        final StringBuilder type = new StringBuilder();
        final StringBuilder value = new StringBuilder();
        boolean isNullValue = false;
        AttributeParsingState state = AttributeParsingState.Spaces;
        for (final char c : line.toCharArray())
        {
            switch (state)
            {
                case Spaces:
                    switch (c)
                    {
                        case ' ':
                            break;
                        case '0':
                            state = AttributeParsingState.HexKey;
                            key.append(c);
                            break;
                        case '"':
                            state = AttributeParsingState.StringKey;
                            break;
                        default:
                            throw new Error(String.format(template, c, state));
                    }
                    break;
                case HexKey:
                    switch (c)
                    {
                        case ' ':
                            state = AttributeParsingState.BeforeType;
                            break;
                        case 'x':
                        case '0':
                        case '1':
                        case '2':
                        case '3':
                        case '4':
                        case '5':
                        case '6':
                        case '7':
                        case '8':
                        case '9':
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                            key.append(c);
                            break;
                        default:
                            throw new Error(String.format(template, c, state));
                    }
                    break;
                case StringKey:
                    switch (c)
                    {
                        case '"':
                            state = AttributeParsingState.BeforeType;
                            break;
                        default:
                            key.append(c);
                            break;
                    }
                    break;
                case BeforeType:
                    switch (c)
                    {
                        case '<':
                            state = AttributeParsingState.Type;
                            break;
                        default:
                            throw new Error(String.format(template, c, state));
                    }
                    break;
                case Type:
                    switch (c)
                    {
                        case '>':
                            state = AttributeParsingState.AfterType;
                            break;
                        default:
                            type.append(c);
                            break;
                    }
                    break;
                case AfterType:
                    switch (c)
                    {
                        case '=':
                            state = AttributeParsingState.BeforeValue;
                            break;
                        default:
                            throw new Error(String.format(template, c, state));
                    }
                    break;
                case BeforeValue:
                    switch (c)
                    {
                        case '<':
                            state = AttributeParsingState.NullValue;
                            isNullValue = true;
                            value.append(c);
                            break;
                        case '0':
                            // TODO: check that type was "timedate"
                            state = AttributeParsingState.TimeDateValue;
                            value.append(c);
                            break;
                        case '"':
                            state = AttributeParsingState.StringValue;
                            break;
                        default:
                            throw new Error(String.format(template, c, state));
                    }
                    break;
                case NullValue:
                    switch (c)
                    {
                        case '>':
                            state = AttributeParsingState.ValueFinished;
                            value.append(c);
                            break;
                        case 'N':
                        case 'U':
                        case 'L':
                            value.append(c);
                            break;
                        default:
                            throw new Error(String.format(template, c, state));
                    }
                    break;
                case StringValue:
                    // double quotes aren't escaped, so everything goes in as-is
                    value.append(c);
                    break;
                case TimeDateValue:
                    // we don't care about timedate for now, so just append as-is
                    value.append(c);
                    break;
                case ValueFinished:
                    throw new Error(String.format(template, c, state));
            }
        }
        if (isNullValue)
        {
            destination.put(key.toString(), null);
        }
        else if ("blob".equals(type.toString()))
        {
            final int lastCharIndex = value.length() - 1;
            value.deleteCharAt(lastCharIndex);
            destination.put(key.toString(), value.toString());
        }
        // TODO: else if ("timedate".equals(type))
        // TODO: else if ("uint32".equals(type))
        // TODO: else if ("sint32".equals(type))
    }

    @Override
    public void delete(final String targetName)
    {
        final String serviceName = createServiceName(targetName);
        throw new NotImplementedException(421274);
    }

    @Override
    public Credential readCredentials(final String targetName)
    {
        final String serviceName = createServiceName(targetName);
        throw new NotImplementedException(421274);
    }

    @Override
    public Token readToken(final String targetName)
    {
        final String serviceName = createServiceName(targetName);
        throw new NotImplementedException(421274);
    }

    @Override
    public void writeCredential(final String targetName, final Credential credentials)
    {
        final String serviceName = createServiceName(targetName);
        throw new NotImplementedException(421274);
    }

    @Override
    public void writeToken(final String targetName, final Token token)
    {
        final String serviceName = createServiceName(targetName);
        throw new NotImplementedException(421274);
    }
}
