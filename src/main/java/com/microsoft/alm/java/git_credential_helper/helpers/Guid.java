package com.microsoft.alm.java.git_credential_helper.helpers;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Guid
{
    public static final UUID Empty = UUID.fromString("00000000-0000-0000-0000-000000000000");

    // Inspired by: http://stackoverflow.com/a/1055668/
    public static byte[] toBytes(final UUID value)
    {
        final ByteBuffer bytes = ByteBuffer.allocate(16);

        final long mostSignificantBits = value.getMostSignificantBits();

        final int upperMsb = (int) (mostSignificantBits >> 32);
        bytes.putInt(Integer.reverseBytes(upperMsb));

        final int lowerMsb = (int) (mostSignificantBits & 0xffffffff);
        final short firstLowerMsb = (short) (lowerMsb >> 16);
        bytes.putShort(Short.reverseBytes(firstLowerMsb));
        final short secondLowerMsb = (short) (lowerMsb & 0xffff);
        bytes.putShort(Short.reverseBytes(secondLowerMsb));

        bytes.putLong(value.getLeastSignificantBits());

        return bytes.array();
    }
}
