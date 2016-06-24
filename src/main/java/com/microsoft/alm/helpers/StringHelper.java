// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.nio.charset.Charset;

public class StringHelper
{
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static final String Empty = "";

    public static boolean endsWithIgnoreCase(final String haystack, final String needle)
    {
        if (haystack == null)
            throw new IllegalArgumentException("Parameter 'haystack' is null.");
        if (needle == null)
            throw new IllegalArgumentException("Parameter 'needle' is null.");

        final int nl = needle.length();
        final int hl = haystack.length();
        if (nl == hl)
        {
            return haystack.equalsIgnoreCase(needle);
        }

        if (nl > hl)
        {
            return false;
        }

        // Inspired by https://stackoverflow.com/a/19154150/
        final int toffset = hl - nl;
        return haystack.regionMatches(true, toffset, needle, 0, nl);
    }

    /**
     * Reports the zero-based index of the first occurrence in s
     * of any character in a specified array of Unicode characters.
     *
     * @param s     The string to search.
     * @param anyOf A Unicode character array containing one or more characters to seek.
     * @return      The zero-based index position of the first occurrence in s
     *              where any character in anyOf was found; -1 if no character in anyOf was found.
     */
    public static int indexOfAny(final String s, final char[] anyOf)
    {
        for (final char c : anyOf)
        {
            final int i = s.indexOf(c);
            if (i != -1)
            {
                return i;
            }
        }
        return -1;
    }

    public static boolean isNullOrEmpty(final String s)
    {
        return null == s || (s.length() == 0);
    }

    public static boolean isNullOrWhiteSpace(final String s)
    {
        return null == s || (s.trim().length() == 0);
    }

    /**
     * Concatenates the specified elements of a string array,
     * using the specified separator between each element.
     *
     * @param separator  The string to use as a separator.
     *                   separator is included in the returned string only if value has more than one element.
     * @param value      An array that contains the elements to concatenate.
     * @return           A string that consists of the strings in value delimited by the separator string.
     *                   If value is an empty array, the method returns {@link StringHelper#Empty}.
     */
    public static String join(final String separator, final String[] value)
    {
        return join(separator, value, 0, value.length);
    }

    /**
     * Concatenates the specified elements of a string array,
     * using the specified separator between each element.
     *
     * @param separator  The string to use as a separator.
     *                   separator is included in the returned string only if value has more than one element.
     * @param value      An array that contains the elements to concatenate.
     * @param startIndex The first element in value to use.
     * @param count      The number of elements of value to use.
     * @return           A string that consists of the strings in value delimited by the separator string.
     *                   -or-
     *                   {@link StringHelper#Empty} if count is zero, value has no elements,
     *                   or separator and all the elements of value are {@link StringHelper#Empty}.
     */
    public static String join(final String separator, final String[] value, final int startIndex, final int count)
    {
        return join(separator, value, startIndex, count, null);
    }

    /**
     * Concatenates the specified elements of a string array,
     * using the specified separator between each element.
     *
     * @param separator  The string to use as a separator.
     *                   separator is included in the returned string only if value has more than one element.
     * @param value      An array that contains the elements to concatenate.
     * @param startIndex The first element in value to use.
     * @param count      The number of elements of value to use.
     * @param processor  A callback that gets to intercept and modify elements before they are inserted.
     * @return           A string that consists of the strings in value delimited by the separator string.
     *                   -or-
     *                   {@link StringHelper#Empty} if count is zero, value has no elements,
     *                   or separator and all the elements of value are {@link StringHelper#Empty}.
     */
    public static String join(final String separator, final String[] value, final int startIndex, final int count, final Func<String, String> processor)
    {
        if (value == null)
            throw new IllegalArgumentException("value is null");
        if (startIndex < 0)
            throw new IllegalArgumentException("startIndex is less than 0");
        if (count < 0)
            throw new IllegalArgumentException("count is less than 0");
        if (startIndex + count > value.length)
            throw new IllegalArgumentException("startIndex + count is greater than the number of elements in value");

        // "If separator is null, an empty string ( String.Empty) is used instead."
        final String sep = ObjectExtensions.coalesce(separator, StringHelper.Empty);

        final StringBuilder result = new StringBuilder();

        if (value.length > 0 && count > 0)
        {
            String element = ObjectExtensions.coalesce(value[startIndex], StringHelper.Empty);
            if (processor != null)
            {
                element = processor.call(element);
            }
            result.append(element);
            for (int i = startIndex + 1; i < startIndex + count; i++)
            {
                result.append(sep);
                element = ObjectExtensions.coalesce(value[i], StringHelper.Empty);
                if (processor != null)
                {
                    element = processor.call(element);
                }
                result.append(element);
            }
        }

        return result.toString();
    }

    /**
     * Removes all trailing occurrences of a set of characters specified in an array from s.
     *
     * @param s         The string to trim from.
     * @param trimChars An array of Unicode characters to remove, or null.
     * @return          The string that remains after all occurrences of the characters in the
     *                  trimChars parameter are removed from the end of s.
     *                  If trimChars is null or an empty array,
     *                  Unicode white-space characters are removed instead.
     *                  If no characters can be trimmed from s, the method returns s.
     */
    public static String trimEnd(final String s, final char... trimChars)
    {
        int len = s.length();
        if (trimChars == null || trimChars.length == 0)
            return trimEnd(s);
        while (len > 0)
        {
            final char current = s.charAt(len - 1);
            boolean found = false;
            for (final char c : trimChars)
            {
                if (current == c)
                {
                    found = true;
                    break;
                }
            }
            if (!found)
                break;
            len--;
        }
        return (len < s.length()) ? s.substring(0, len) : s;
    }

    public static String trimEnd(final String s)
    {
        int len = s.length();
        while (len > 0)
        {
            final char current = s.charAt(len - 1);
            if (!Character.isWhitespace(current))
                break;
            len--;
        }
        return (len < s.length()) ? s.substring(0, len) : s;
    }

    /**
     * Encodes all the characters in the specified string into a sequence of UTF-8 bytes.
     * @param value The string containing the characters to encode.
     * @return      A byte array containing the results of encoding the specified set of characters.
     */
    public static byte[] UTF8GetBytes(final String value)
    {
        final byte[] result = value.getBytes(UTF8);
        return result;
    }

    /**
     * Decodes all the bytes in the specified byte array into a string.
     *
     * @param bytes The byte array containing the sequence of bytes to decode.
     * @return      A string that contains the results of decoding the specified sequence of bytes.
     */
    public static String UTF8GetString(final byte[] bytes)
    {
        final String result = new String(bytes, UTF8);
        return result;
    }

    /**
     * Decodes a range of bytes from a byte array into a string.
     *
     * @param bytes The byte array containing the sequence of bytes to decode.
     * @param index The index of the first byte to decode.
     * @param count The number of bytes to decode.
     * @return      A string that contains the results of decoding the specified sequence of bytes.
     */
    public static String UTF8GetString(final byte[] bytes, final int index, final int count)
    {
        final String result = new String(bytes, index, count, UTF8);
        return result;
    }
}
