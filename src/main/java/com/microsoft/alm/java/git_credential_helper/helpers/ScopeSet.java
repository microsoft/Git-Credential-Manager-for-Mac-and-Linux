package com.microsoft.alm.java.git_credential_helper.helpers;

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
        return this.containsAll(Arrays.asList(items));
    }
}
