package com.microsoft.alm.java.git_credential_helper.helpers;

public class StringHelper
{
    public static final String Empty = "";

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
     * @param startIndex The first element in value to use.
     * @param count      The number of elements of value to use.
     * @return           A string that consists of the strings in value delimited by the separator string.
     *                   -or-
     *                   {@link StringHelper#Empty} if count is zero, value has no elements,
     *                   or separator and all the elements of value are {@link StringHelper#Empty}.
     */
    public static String join(final String separator, final String[] value, final int startIndex, final int count)
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
            result.append(ObjectExtensions.coalesce(value[startIndex], StringHelper.Empty));
            for (int i = startIndex + 1; i < startIndex + count; i++)
            {
                result.append(sep);
                result.append(ObjectExtensions.coalesce(value[i], StringHelper.Empty));
            }
        }

        return result.toString();
    }
}
