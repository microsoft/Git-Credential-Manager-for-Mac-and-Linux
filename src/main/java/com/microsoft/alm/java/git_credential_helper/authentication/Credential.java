package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

/**
 * Credential for user authentication.
 */
public final class Credential extends Secret // TODO: implements IEquatable<Credential>
{
    public static final Credential Empty = new Credential("", "");

    /**
     * Creates a credential object with a username and password pair.
     *
     * @param username The username value of the {@link Credential}.
     * @param password The password value of the {@link Credential}.
     */
    public Credential(final String username, final String password)
    {
        throw new NotImplementedException();
    }
    /**
     * Creates a credential object with only a username.
     *
     * @param username The username value of the {@link Credential}.
     */
    public Credential(final String username)
    {
        this(username, "");
    }

    /**
     * Secret related to the username.
     */
    public final String Password;
    /**
     * Unique identifier of the user.
     */
    public final String Username;

    /**
     * Compares an object to this {@link Credential} for equality.
     *
     * @param obj The object to compare.
     * @return True if equal; false otherwise.
     */
    @Override public boolean equals(final Object obj)
    {
        throw new NotImplementedException();
    }
    /**
     * Gets a hash code based on the contents of the {@link Credential}.
     *
     * @return 32-bit hash code.
     */
    @Override public int hashCode()
    {
        throw new NotImplementedException();
    }

    static void validate(final Credential credentials)
    {
        throw new NotImplementedException();
    }

    /**
     * Compares two credentials for equality.
     *
     * @param credential1 Credential to compare.
     * @param credential2 Credential to compare.
     * @return True if equal; false otherwise.
     */
    public static boolean operatorEqual(final Credential credential1, final Credential credential2)
    {
        throw new NotImplementedException();
    }
    /**
     * Compares two credentials for inequality.
     *
     * @param credential1 Credential to compare.
     * @param credential2 Credential to compare.
     * @return False if equal; true otherwise.
     */
    public static boolean operatorNotEqual(final Credential credential1, final Credential credential2)
    {
        throw new NotImplementedException();
    }
}
