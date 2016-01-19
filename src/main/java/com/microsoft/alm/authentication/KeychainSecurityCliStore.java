// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.oauth2.useragent.subprocess.DefaultProcessFactory;
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcessFactory;

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
