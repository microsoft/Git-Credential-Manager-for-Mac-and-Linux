// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.util.Iterator;

public class IteratorExtensions
{
    public static <T> T firstOrDefault(final Iterator<T> iterator)
    {
        if (iterator.hasNext())
        {
            return iterator.next();
        }
        else
        {
            return null;
        }
    }
}
