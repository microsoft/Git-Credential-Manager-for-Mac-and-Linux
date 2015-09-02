package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;
import com.microsoft.alm.java.git_credential_helper.helpers.ScopeSet;

public abstract class TokenScope // TODO: implements IEquatable<TokenScope>
{
    protected TokenScope(final String value)
    {
        throw new NotImplementedException();
    }

    protected TokenScope(final String[] values)
    {
        throw new NotImplementedException();
    }

    protected TokenScope(final ScopeSet set)
    {
        throw new NotImplementedException();
    }

    public String getValue() { throw new NotImplementedException(); }

    protected final String[] _scopes;

    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    @Override public boolean equals(final Object obj)
    {
        throw new NotImplementedException();
    }

    @Override public int hashCode()
    {
        throw new NotImplementedException();
    }

    @Override public String toString()
    {
        return getValue();
    }

    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static boolean operatorEqual(final TokenScope scope1, final TokenScope scope2)
    {
        throw new NotImplementedException();
    }
    // TODO: [MethodImpl(MethodImplOptions.AggressiveInlining)]
    public static boolean operatorNotEqual(final TokenScope scope1, final TokenScope scope2)
    {
        throw new NotImplementedException();
    }
}
