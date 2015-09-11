package com.microsoft.alm.java.git_credential_helper.helpers;

import com.microsoft.alm.java.git_credential_helper.authentication.Credential;
import com.microsoft.alm.java.git_credential_helper.authentication.Token;

public interface ISecureStore
{
    void delete(final String targetName);

    Credential readCredentials(final String targetName);

    Token readToken(String targetName);

    void writeCredential(String targetName, Credential credentials);

    void writeToken(String targetName, Token token);
}
