package com.microsoft.alm.helpers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Guid
{
    public static final UUID Empty = UUID.fromString("00000000-0000-0000-0000-000000000000");

    // http://stackoverflow.com/a/28628209/
    public static UUID fromBytes(final byte[] b)
    {
        ByteBuffer source = ByteBuffer.wrap(b);
        ByteBuffer target = ByteBuffer.allocate(16).
                order(ByteOrder.LITTLE_ENDIAN).
                putInt(source.getInt()).
                putShort(source.getShort()).
                putShort(source.getShort()).
                order(ByteOrder.BIG_ENDIAN).
                putLong(source.getLong());
        target.rewind();
        return new UUID(target.getLong(), target.getLong());
    }

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

    public static boolean tryParse(final String input, final AtomicReference<UUID> result)
    {
        try
        {
            result.set(UUID.fromString(input));
            return true;
        }
        catch (final IllegalArgumentException ignored)
        {
            return false;
        }
    }
}
