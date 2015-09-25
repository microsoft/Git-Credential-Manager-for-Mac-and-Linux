package com.microsoft.alm.helpers;

/**
 * Equivalent to .NET's BitConverter class
 */
public class BitConverter
{
    /**
     * Converts the numeric value of each element of a specified array of bytes
     * to its equivalent hexadecimal string representation.
     *
     * @param bytes An array of bytes.
     * @return A string of hexadecimal pairs separated by hyphens,
     * where each pair represents the corresponding element in bytes;
     * for example, "7F-2C-4A-00".
     */
    public static String toString(final byte[] bytes)
    {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final byte b : bytes)
        {
            if (!first)
            {
                sb.append('-');
            }
            first = false;
            final String pair = String.format("%1$X", b);
            if (pair.length() < 2)
            {
                sb.append('0');
            }
            sb.append(pair);
        }
        return sb.toString();
    }
}
