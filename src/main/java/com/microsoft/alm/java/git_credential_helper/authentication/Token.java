package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.Debug;
import com.microsoft.alm.java.git_credential_helper.helpers.Guid;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;
import com.microsoft.alm.java.git_credential_helper.helpers.Trace;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A security token, usually acquired by some authentication and identity services.
 */
public class Token extends Secret // TODO: implements IEquatable<Token>
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

    UUID targetIdentity = Guid.Empty;
    /**
     * The guid form Identity of the target
     */
    public UUID getTargetIdentity()
    {
        return targetIdentity;
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
            Trace.writeLine("   token deserialization error");
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
        catch(final Throwable t)
        {
            Trace.writeLine("   token serialization error");
        }

        return byteReference.get() != null;
    }

    private static final int PasswordMaxLength = 2047;
    static void validate(final Token token)
    {
        if (token == null)
            throw new IllegalArgumentException("The `token` parameter is null or invalid.");
        if (StringHelper.isNullOrWhiteSpace(token.Value))
            throw new IllegalArgumentException("The value of the `token` cannot be null or empty.");
        if (token.Value.length() > PasswordMaxLength)
            throw new IllegalArgumentException(String.format("The value of the `token` cannot be longer than %1$d characters.", PasswordMaxLength));
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
