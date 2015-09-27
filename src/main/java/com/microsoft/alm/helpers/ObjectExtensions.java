// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

public class ObjectExtensions
{
    /**
     * Equivalent to the C# null-coalescing operator '??'.
     *
     * @param <T>            the type of both values.
     * @param maybeNullValue the value that might be null.
     * @param nonNullValue   the value to use if the other one is null.
     * @return               maybeNullValue if maybeNullValue is not null; otherwise it returns nonNullValue.
     */
    public static <T> T coalesce(final T maybeNullValue, final T nonNullValue)
    {
        return maybeNullValue == null ? nonNullValue : maybeNullValue;
    }
}
