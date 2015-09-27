// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

public interface ISecureStore
{
    void delete(final String targetName);

    Credential readCredentials(final String targetName);

    Token readToken(String targetName);

    void writeCredential(String targetName, Credential credentials);

    void writeToken(String targetName, Token token);
}
