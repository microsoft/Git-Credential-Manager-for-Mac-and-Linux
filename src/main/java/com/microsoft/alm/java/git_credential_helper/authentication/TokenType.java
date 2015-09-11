package com.microsoft.alm.java.git_credential_helper.authentication;

public enum TokenType
{
    Unknown(null),
    /**
     * Azure Directory Access Token
     */
    Access("Azure Directory Access Token"),
    /**
     * Azure Directory Refresh Token
     */
    Refresh("Azure Directory Refresh Token"),
    /**
     * Personal Access Token, can be compact or not.
     */
    Personal("Personal Access Token"),
    /**
     * Federated Authentication (aka FedAuth) Token
     */
    Federated("Federated Authentication Token"),
    /**
     * Used only for testing
     */
    Test("Test-only Token"),
    ;

    private final String description;

    private TokenType(final String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }
}
