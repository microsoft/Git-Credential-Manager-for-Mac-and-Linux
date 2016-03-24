// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Debug;
import com.microsoft.alm.helpers.Guid;
import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.helpers.Trace;
import com.microsoft.alm.helpers.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A security token, usually acquired by some authentication and identity services.
 */
public class Token extends Secret
{
    private static final int sizeofTokenType = 4;
    private static final int sizeofGuid = 16;

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

    UUID targetIdentity = Guid.Empty;

    public static Token fromXml(final Node tokenNode)
    {
        Token value;

        String tokenValue = null;
        TokenType tokenType = null;
        UUID targetIdentity = Guid.Empty;

        final NodeList propertyNodes = tokenNode.getChildNodes();
        for (int v = 0; v < propertyNodes.getLength(); v++)
        {
            final Node propertyNode = propertyNodes.item(v);
            final String propertyName = propertyNode.getNodeName();
            if ("Type".equals(propertyName))
            {
                tokenType = TokenType.valueOf(TokenType.class, XmlHelper.getText(propertyNode));
            }
            else if ("Value".equals(propertyName))
            {
                tokenValue = XmlHelper.getText(propertyNode);
            }
            else if ("targetIdentity".equals(propertyName))
            {
                targetIdentity = UUID.fromString(XmlHelper.getText(propertyNode));
            }
        }
        value = new Token(tokenValue, tokenType);
        value.setTargetIdentity(targetIdentity);
        return value;
    }

    public Element toXml(final Document document)
    {
        final Element valueNode = document.createElement("value");

        final Element typeNode = document.createElement("Type");
        final Text typeValue = document.createTextNode(this.Type.toString());
        typeNode.appendChild(typeValue);
        valueNode.appendChild(typeNode);

        final Element tokenValueNode = document.createElement("Value");
        final Text valueValue = document.createTextNode(this.Value);
        tokenValueNode.appendChild(valueValue);
        valueNode.appendChild(tokenValueNode);

        if (!Guid.Empty.equals(this.getTargetIdentity()))
        {
            final Element targetIdentityNode = document.createElement("targetIdentity");
            final Text targetIdentityValue = document.createTextNode(this.getTargetIdentity().toString());
            targetIdentityNode.appendChild(targetIdentityValue);
            valueNode.appendChild(targetIdentityNode);
        }
        return valueNode;
    }

    /**
     * @return The guid form Identity of the target
     */
    public UUID getTargetIdentity()
    {
        return targetIdentity;
    }

    public void setTargetIdentity(final UUID targetIdentity)
    {
        this.targetIdentity = targetIdentity;
    }

    /**
     * Compares an object to this {@link Token} for equality.
     *
     * @param obj The object to compare.
     * @return True is equal; false otherwise.
     */
    @Override public boolean equals(final Object obj)
    {
        return operatorEquals(this, obj instanceof Token ? ((Token) obj) : null);
    }
    // PORT NOTE: Java doesn't support a specific overload (as per IEquatable<T>)
    /**
     * Gets a hash code based on the contents of the token.
     *
     * @return 32-bit hash code.
     */
    @Override public int hashCode()
    {
        // PORT NOTE: Java doesn't have unchecked blocks; the default behaviour is apparently equivalent.
        {
            return Type.getValue() * Value.hashCode();
        }
    }
    /**
     * Converts the token to a human friendly string.
     *
     * @return Humanish name of the token.
     */
    @Override public String toString()
    {
        final AtomicReference<String> value = new AtomicReference<String>();
        if (getFriendlyNameFromType(Type, value))
            return value.get();
        else
            return super.toString();
    }

    public void contributeHeader(final Map<String, String> headers)
    {
        // different types of tokens are packed differently
        switch (Type)
        {
            case Access:
                final String prefix = "Bearer";
                headers.put("Authorization", prefix + " " + Value);
                break;
            case Federated:
                throw new NotImplementedException(449222);
            default:
                final String template = "Tokens of type '%1$s' cannot be used for headers.";
                final String message = String.format(template, Type);
                throw new IllegalStateException(message);
        }
    }

    static boolean deserialize(final byte[] bytes, final TokenType type, final AtomicReference<Token> tokenReference)
    {
        Debug.Assert(bytes != null, "The bytes parameter is null");
        Debug.Assert(bytes.length > 0, "The bytes parameter is too short");
        Debug.Assert(type != null, "The type parameter is invalid");

        tokenReference.set(null);

        try
        {
            final int preamble = sizeofTokenType + sizeofGuid;

            if (bytes.length > preamble)
            {
                TokenType readType;
                UUID targetIdentity;

                final ByteBuffer p = ByteBuffer.wrap(bytes); // PORT NOTE: ByteBuffer is closest to "fixed"
                {
                    readType = TokenType.fromValue(Integer.reverseBytes(p.getInt()));
                    byte[] guidBytes = new byte[16];
                    p.get(guidBytes);
                    targetIdentity = Guid.fromBytes(guidBytes);
                }

                if (readType == type)
                {
                    final String value = StringHelper.UTF8GetString(bytes, preamble, bytes.length - preamble);

                    if (!StringHelper.isNullOrWhiteSpace(value))
                    {
                        tokenReference.set(new Token(value, type));
                        tokenReference.get().targetIdentity = targetIdentity;
                    }
                }
            }

            // if value hasn't been set yet, fall back to old format decode
            if (tokenReference.get() == null)
            {
                final String value = StringHelper.UTF8GetString(bytes);

                if (!StringHelper.isNullOrWhiteSpace(value))
                {
                    tokenReference.set(new Token(value, type));
                }
            }
        }
        catch (final Throwable throwable)
        {
            throw new Error("   token deserialization error", throwable);
        }

        return tokenReference.get() != null;
    }

    static boolean serialize(final Token token, final AtomicReference<byte[]> byteReference)
    {
        Debug.Assert(token != null, "The token parameter is null");
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(token.Value), "The token.Value is invalid");

        byteReference.set(null);

        try
        {
            final byte[] utf8bytes = StringHelper.UTF8GetBytes(token.Value);
            final ByteBuffer bytes = ByteBuffer.allocate(utf8bytes.length + sizeofTokenType + sizeofGuid);

            // PORT NOTE: "fixed" block pointer arithmetic and casting avoided
            {
                bytes.putInt(Integer.reverseBytes(token.Type.getValue()));
                bytes.put(Guid.toBytes(token.targetIdentity));
            }

            bytes.put(utf8bytes);
            byteReference.set(bytes.array());
        }
        catch (final Throwable t)
        {
            throw new Error("   token serialization error", t);
        }

        return byteReference.get() != null;
    }

    static void validate(final Token token)
    {
        if (token == null)
            throw new IllegalArgumentException("The `token` parameter is null or invalid.");
        if (StringHelper.isNullOrWhiteSpace(token.Value))
            throw new IllegalArgumentException("The value of the `token` cannot be null or empty.");
        if (token.Value.length() > Global.PasswordMaxLength)
            throw new IllegalArgumentException(String.format("The value of the `token` cannot be longer than %1$d characters.", Global.PasswordMaxLength));
    }

    /**
     * Explicitly casts a personal access token token into a set of credentials
     *
     * @param token The {@link Token} to convert.
     * @return A corresponding {@link Credential} instance.
     * @throws IllegalArgumentException if the {@link Token#Type} is not {@link TokenType#Personal}.
     */
    // PORT NOTE: Java doesn't have cast operator overloading
    public static Credential toCredential(final Token token)
    {
        if (token == null)
            return null;

        if (token.Type != TokenType.Personal)
            throw new IllegalArgumentException("Cannot convert " + token.toString() + " to credentials");

        return new Credential(token.toString(), token.Value);
    }

    /**
     * Compares two tokens for equality.
     *
     * @param token1 Token to compare.
     * @param token2 Token to compare.
     * @return True if equal; false otherwise.
     */
    public static boolean operatorEquals(final Token token1, final Token token2)
    {
        if (token1 == token2)
            return true;
        if ((token1 == null) || (null == token2))
            return false;

        return token1.Type == token2.Type
                && token1.Value.equalsIgnoreCase(token2.Value);
    }
    /**
     * Compares two tokens for inequality.
     *
     * @param token1 Token to compare.
     * @param token2 Token to compare.
     * @return False if equal; true otherwise.
     */
    public static boolean operatorNotEquals(final Token token1, final Token token2)
    {
        return !operatorEquals(token1, token2);
    }



}
