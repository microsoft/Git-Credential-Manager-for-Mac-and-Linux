// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.NotImplementedException;

public class KeychainSecurityCliStore implements ISecureStore
{

    @Override
    public void delete(final String targetName)
    {
        throw new NotImplementedException(421274);
    }

    @Override
    public Credential readCredentials(final String targetName)
    {
        throw new NotImplementedException(421274);
    }

    @Override
    public Token readToken(final String targetName)
    {
        throw new NotImplementedException(421274);
    }

    @Override
    public void writeCredential(final String targetName, final Credential credentials)
    {
        throw new NotImplementedException(421274);
    }

    @Override
    public void writeToken(final String targetName, final Token token)
    {
        throw new NotImplementedException(421274);
    }
}
