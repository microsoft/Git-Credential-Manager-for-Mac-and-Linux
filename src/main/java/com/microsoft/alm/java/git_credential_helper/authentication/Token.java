package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.Debug;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A security token, usually acquired by some authentication and identity services.
 */
public class Token extends Secret // TODO: implements IEquatable<Token>
{
    public static boolean getFriendlyNameFromType(final TokenType type, final AtomicReference<String> name)
    {
        // PORT NOTE: Java doesn't have the concept of out-of-range enums

        name.set(null);

        name.set(type.getDescription() == null
                ? type.toString()
                : type.getDescription());

        return name.get() != null;
    }

    public static boolean getTypeFromFriendlyName(final String name, final AtomicReference<TokenType> type)
    {
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(name), "The name parameter is null or invalid");

        type.set(TokenType.Unknown);

        for (final TokenType value : EnumSet.allOf(TokenType.class))
        {
            type.set(value);

            AtomicReference<String> typename = new AtomicReference<String>();
            if (getFriendlyNameFromType(type.get(), typename))
            {
                if (name.equalsIgnoreCase(typename.get()))
                    return true;
            }
        }

        return false;
    }

    //http://blog.bdoughan.com/2010/12/jaxb-and-immutable-objects.html?showComment=1296031142997#c584069422380571931
    @SuppressWarnings("unused" /* Used by JAXB's serialization */)
    private Token()
    {
        this.Type = null;
        this.Value = null;
    }

    public Token(final String value, final TokenType type)
    {
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(value), "The value parameter is null or invalid");
        // PORT NOTE: Java doesn't have the concept of out-of-range enums

        this.Type = type;
        this.Value = value;
    }

    Token(final String value, final String typeName)
    {
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(value), "The value parameter is null or invalid");
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(typeName), "The typeName parameter is null or invalid");

        AtomicReference<TokenType> type = new AtomicReference<TokenType>();
        if (!getTypeFromFriendlyName(typeName, type))
        {
            throw new IllegalArgumentException("Unexpected token type '" + typeName + "' encountered");
        }
        this.Type = type.get();
        this.Value = value;
    }

    // PORT NOTE: ADAL-specific constructor omitted

    /**
     * The type of the security token.
     */
    public final TokenType Type;
    /**
     * The raw contents of the token.
     */
    public final String Value;
}
