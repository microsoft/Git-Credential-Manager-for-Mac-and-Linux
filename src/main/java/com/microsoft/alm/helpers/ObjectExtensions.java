// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

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
