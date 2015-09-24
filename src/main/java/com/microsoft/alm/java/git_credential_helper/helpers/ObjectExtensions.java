package com.microsoft.alm.java.git_credential_helper.helpers;

public class ObjectExtensions
{
    /**
     * Equivalent to the C# null-coalescing operator '??'.
     */
    public static <T> T coalesce(final T maybeNullValue, final T nonNullValue)
    {
        return maybeNullValue == null ? nonNullValue : maybeNullValue;
    }
}
