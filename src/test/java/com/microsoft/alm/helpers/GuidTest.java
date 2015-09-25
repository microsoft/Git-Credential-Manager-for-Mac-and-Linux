package com.microsoft.alm.helpers;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

public class GuidTest
{
    @Test public void fromBytes_allOnes() throws Exception
    {
        byte[] input =
        {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        };

        final UUID actual = Guid.fromBytes(input);

        Assert.assertEquals("ffffffff-ffff-ffff-ffff-ffffffffffff", actual.toString());
    }

    @Test public void fromBytes_allZeroes() throws Exception
    {
        byte[] input =
        {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        };

        final UUID actual = Guid.fromBytes(input);

        Assert.assertEquals("00000000-0000-0000-0000-000000000000", actual.toString());
    }

    @Test public void fromBytes_typical() throws Exception
    {
        byte[] input =
        {
            (byte) 0x3e, (byte) 0x28, (byte) 0x02, (byte) 0x86,
            (byte) 0xd6, (byte) 0x2e,
            (byte) 0x60, (byte) 0x49,
            (byte) 0xad, (byte) 0xaa,
            (byte) 0x97, (byte) 0xbe, (byte) 0x7d, (byte) 0x99, (byte) 0x13, (byte) 0xde,
        };

        final UUID actual = Guid.fromBytes(input);

        Assert.assertEquals("8602283e-2ed6-4960-adaa-97be7d9913de", actual.toString());
    }

    @Test public void toBytes_allOnes() throws Exception
    {
        final UUID value = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        final byte[] actual = Guid.toBytes(value);

        byte[] expected =
        {
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff,
            (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
        };
        assertArrayEquals(expected, actual);
    }

    @Test public void toBytes_allZeroes() throws Exception
    {
        final byte[] actual = Guid.toBytes(Guid.Empty);

        byte[] expected =
        {
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00,
            0x00, 0x00,
            0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        };
        assertArrayEquals(expected, actual);
    }

    @Test public void toBytes_typical() throws Exception
    {
        final UUID value = UUID.fromString("8602283e-2ed6-4960-adaa-97be7d9913de");
        final byte[] actual = Guid.toBytes(value);

        byte[] expected =
        {
            (byte) 0x3e, (byte) 0x28, (byte) 0x02, (byte) 0x86,
            (byte) 0xd6, (byte) 0x2e,
            (byte) 0x60, (byte) 0x49,
            (byte) 0xad, (byte) 0xaa,
            (byte) 0x97, (byte) 0xbe, (byte) 0x7d, (byte) 0x99, (byte) 0x13, (byte) 0xde,
        };
        assertArrayEquals(expected, actual);
    }

    private static void assertArrayEquals(byte[] expected, byte[] actual)
    {
        if (!Arrays.equals(expected, actual))
        {
            final String template = "Arrays were different.\n" +
                    "Expected :%1$s\n" +
                    "Actual   :%2$s";
            final String expectedHex = BitConverter.toString(expected);
            final String actualHex = BitConverter.toString(actual);
            final String message = String.format(template, expectedHex, actualHex);
            Assert.fail(message);
        }
    }
}
