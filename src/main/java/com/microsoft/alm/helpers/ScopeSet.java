// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import java.util.Arrays;
import java.util.TreeSet;

public class ScopeSet extends TreeSet<String>
{
    public void unionWith(final String[] items)
    {
        this.addAll(Arrays.asList(items));
    }

    public void intersectWith(final String[] items)
    {
        this.retainAll(Arrays.asList(items));
    }

    public boolean setEquals(final String[] items)
    {
        return this.size() == items.length
                && this.containsAll(Arrays.asList(items));
    }
}
