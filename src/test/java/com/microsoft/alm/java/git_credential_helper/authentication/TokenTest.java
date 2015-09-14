package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.BitConverter;
import com.microsoft.alm.java.git_credential_helper.helpers.Guid;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class TokenTest
{
    @Test public void deserialize_newFormat()
    {
        byte[] input =
        {
            (byte) 0x05, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x3e, (byte) 0x28, (byte) 0x02, (byte) 0x86,
            (byte) 0xd6, (byte) 0x2e,
            (byte) 0x60, (byte) 0x49,
            (byte) 0xad, (byte) 0xaa,
            (byte) 0x97, (byte) 0xbe, (byte) 0x7d, (byte) 0x99, (byte) 0x13, (byte) 0xde,
            (byte) 0x31,
        };

        assertDeserialize(TokenType.Test, "1", "8602283e-2ed6-4960-adaa-97be7d9913de", input);
    }

    @Test public void deserialize_oldFormat()
    {
        byte[] input =
        {
            (byte) 0x31,
        };

        assertDeserialize(TokenType.Test, "1", Guid.Empty.toString(), input);
    }

    private static void assertDeserialize(final TokenType expectedTokenType, final String expectedValue, final String expectedGuid, final byte[] input)
    {
        final AtomicReference<Token> tokenReference = new AtomicReference<Token>();
        final boolean actualResult = Token.deserialize(input, expectedTokenType, tokenReference);

        Assert.assertTrue(actualResult);
        final Token actualToken = tokenReference.get();
        Assert.assertNotNull(actualToken);
        Assert.assertEquals(expectedValue, actualToken.Value);
        Assert.assertEquals(expectedTokenType, actualToken.Type);
        final UUID expectedTargetIdentity = UUID.fromString(expectedGuid);
        Assert.assertEquals(expectedTargetIdentity, actualToken.getTargetIdentity());
    }

    @Test public void serialize_almostAllOnes()
    {
        final Token token = new Token("1", TokenType.Access);
        token.targetIdentity = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

        assertSerialize("01-00-00-00-FF-FF-FF-FF-FF-FF-FF-FF-FF-FF-FF-FF-FF-FF-FF-FF-31", token);
    }

    @Test public void serialize_almostAllZeroes()
    {
        final Token token = new Token("0", TokenType.Unknown);

        assertSerialize("00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-00-30", token);
    }

    @Test public void serialize_typical()
    {
        final Token token = new Token("1", TokenType.Test);
        token.targetIdentity = UUID.fromString("8602283e-2ed6-4960-adaa-97be7d9913de");

        assertSerialize("05-00-00-00-3E-28-02-86-D6-2E-60-49-AD-AA-97-BE-7D-99-13-DE-31", token);
    }

    private static void assertSerialize(String expectedHex, Token token)
    {
        final AtomicReference<byte[]> bytesRef = new AtomicReference<byte[]>();

        final boolean actualResult = Token.serialize(token, bytesRef);

        Assert.assertEquals(true, actualResult);
        final byte[] actualBytes = bytesRef.get();
        Assert.assertNotNull(actualBytes);
        final String actualHex = BitConverter.toString(actualBytes);
        Assert.assertEquals(expectedHex, actualHex);
    }
}
