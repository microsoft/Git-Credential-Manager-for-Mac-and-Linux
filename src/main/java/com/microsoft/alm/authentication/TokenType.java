package com.microsoft.alm.authentication;

import java.util.HashMap;
import java.util.Map;

public enum TokenType
{
    Unknown(null, 0),
    /**
     * Azure Directory Access Token
     */
    Access("Azure Directory Access Token", 1),
    /**
     * Azure Directory Refresh Token
     */
    Refresh("Azure Directory Refresh Token", 2),
    /**
     * Personal Access Token, can be compact or not.
     */
    Personal("Personal Access Token", 3),
    /**
     * Federated Authentication (aka FedAuth) Token
     */
    Federated("Federated Authentication Token", 4),
    /**
     * Used only for testing
     */
    Test("Test-only Token", 5),
    ;

    private static final Map<Integer, TokenType> valueToTokenType;

    static
    {
        valueToTokenType = new HashMap<Integer, TokenType>();
        for (final TokenType value : TokenType.values())
        {
            valueToTokenType.put(value.getValue(), value);
        }
    }

    private final String description;
    private final int value;

    private TokenType(final String description, final int value)
    {
        this.description = description;
        this.value = value;
    }

    public String getDescription()
    {
        return description;
    }

    public int getValue()
    {
        return value;
    }

    public static TokenType fromValue(final int value)
    {
        return valueToTokenType.get(value);
    }
}
