package com.microsoft.alm.java.git_credential_helper.authentication;

public enum TokenType
{
    Unknown,
    /**
     * Azure Directory Access Token
     */
    Access,
    /**
     * Azure Directory Refresh Token
     */
    Refresh,
    /**
     * Personal Access Token, can be compact or not.
     */
    Personal,
    /**
     * Federated Authentication (aka FedAuth) Token
     */
    Federated,
    /**
     * Used only for testing
     */
    Test,
}
