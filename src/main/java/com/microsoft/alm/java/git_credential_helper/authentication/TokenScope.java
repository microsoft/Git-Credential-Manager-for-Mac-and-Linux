package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.ScopeSet;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;

import java.util.Arrays;

public abstract class TokenScope // TODO: implements IEquatable<TokenScope>
{
    private static final String[] EmptyStringArray = new String[0];

    protected TokenScope(final String value)
    {
        if (StringHelper.isNullOrWhiteSpace(value))
        {
            _scopes = new String[0];
        }
        else
        {
            _scopes = new String[1];
            _scopes[0] = value;
        }
    }

    protected TokenScope(final String[] values)
    {
        _scopes = values;
    }

    protected TokenScope(final ScopeSet set)
    {
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        _scopes = set.toArray(EmptyStringArray);
    }

    public String getValue()
    {
        return StringHelper.join(" ", _scopes);
    }

    protected final String[] _scopes;

    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    @Override public boolean equals(final Object obj)
    {
        return operatorEquals(this, obj instanceof TokenScope ? ((TokenScope) obj) : null);
    }

    @Override public int hashCode()
    {
        // largest 31-bit prime (https://msdn.microsoft.com/en-us/library/Ee621251.aspx)
        int hash = 2147483647;

        for (int i = 0; i < _scopes.length; i++)
        {
            // PORT NOTE: Java doesn't have unchecked blocks; the default behaviour is apparently equivalent.
            {
                hash ^= _scopes[i].hashCode();
            }
        }

        return hash;
    }

    @Override public String toString()
    {
        return getValue();
    }

    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static boolean operatorEquals(final TokenScope scope1, final TokenScope scope2)
    {
        if (scope1 == scope2)
            return true;
        if ((scope1 == null) || (null == scope2))
            return false;

        final ScopeSet set = new ScopeSet();
        set.addAll(Arrays.asList(scope1._scopes));
        return set.containsAll(Arrays.asList(scope2._scopes));
    }
    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static boolean operatorNotEquals(final TokenScope scope1, final TokenScope scope2)
    {
        return !operatorEquals(scope1, scope2);
    }
}
