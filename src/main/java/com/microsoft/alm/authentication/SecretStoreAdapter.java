// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.secret.Credential;
import com.microsoft.alm.secret.Token;

public class SecretStoreAdapter implements ISecureStore
{
    private final com.microsoft.alm.storage.SecretStore<Token> tokenSecretStore;
    private final com.microsoft.alm.storage.SecretStore<Credential> credentialSecretStore;

    public SecretStoreAdapter(final com.microsoft.alm.storage.SecretStore<Token> tokenSecretStore, final com.microsoft.alm.storage.SecretStore<Credential> credentialSecretStore)
    {
        if (tokenSecretStore == null)
            throw new IllegalArgumentException("tokenSecretStore can't be null");
        if (credentialSecretStore == null)
            throw new IllegalArgumentException("credentialSecretStore can't be null");

        this.tokenSecretStore = tokenSecretStore;
        this.credentialSecretStore = credentialSecretStore;
    }

    @Override
    public void delete(final String targetName)
    {
        if (tokenSecretStore.get(targetName) != null)
        {
            tokenSecretStore.delete(targetName);
        }
        else if (credentialSecretStore.get(targetName) != null)
        {
            credentialSecretStore.delete(targetName);
        }
    }

    @Override
    public Credential readCredentials(final String targetName)
    {
        return credentialSecretStore.get(targetName);
    }

    @Override
    public Token readToken(final String targetName)
    {
        return tokenSecretStore.get(targetName);
    }

    @Override
    public void writeCredential(final String targetName, final Credential credentials)
    {
        credentialSecretStore.add(targetName, credentials);
    }

    @Override
    public void writeToken(final String targetName, final Token token)
    {
        tokenSecretStore.add(targetName, token);
    }
}
