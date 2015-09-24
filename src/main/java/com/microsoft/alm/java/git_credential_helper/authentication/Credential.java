package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.ObjectExtensions;
import com.microsoft.alm.java.git_credential_helper.helpers.StringHelper;

/**
 * Credential for user authentication.
 */
public final class Credential extends Secret // TODO: implements IEquatable<Credential>
{
    public static final Credential Empty = new Credential(StringHelper.Empty, StringHelper.Empty);

    //http://blog.bdoughan.com/2010/12/jaxb-and-immutable-objects.html?showComment=1296031142997#c584069422380571931
    @SuppressWarnings("unused" /* Used by JAXB's serialization */)
    private Credential()
    {
        this.Username = null;
        this.Password = null;
    }

    /**
     * Creates a credential object with a username and password pair.
     *
     * @param username The username value of the {@link Credential}.
     * @param password The password value of the {@link Credential}.
     */
    public Credential(final String username, final String password)
    {
        this.Username = ObjectExtensions.coalesce(username, StringHelper.Empty);
        this.Password = ObjectExtensions.coalesce(password, StringHelper.Empty);
    }
    /**
     * Creates a credential object with only a username.
     *
     * @param username The username value of the {@link Credential}.
     */
    public Credential(final String username)
    {
        this(username, StringHelper.Empty);
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
        return operatorEquals(this, obj instanceof Credential ? ((Credential) obj) : null);
    }
    // PORT NOTE: Java doesn't support a specific overload (as per IEquatable<T>)
    /**
     * Gets a hash code based on the contents of the {@link Credential}.
     *
     * @return 32-bit hash code.
     */
    @Override public int hashCode()
    {
        // PORT NOTE: Java doesn't have unchecked blocks; the default behaviour is apparently equivalent.
        {
            return Username.hashCode() + 7 * Password.hashCode();
        }
    }

    static void validate(final Credential credentials)
    {
        if (credentials == null)
            throw new IllegalArgumentException("The credentials parameter cannot be null");
        if (credentials.Password.length() > Global.PasswordMaxLength)
            throw new IllegalArgumentException(String.format("The Password field of the credentials parameter cannot be longer than %1$d characters.", Global.PasswordMaxLength));
        if (credentials.Username.length() > Global.UsernameMaxLength)
            throw new IllegalArgumentException(String.format("The Username field of the credentials parameter cannot be longer than %1$d characters.", Global.UsernameMaxLength));
    }

    /**
     * Compares two credentials for equality.
     *
     * @param credential1 Credential to compare.
     * @param credential2 Credential to compare.
     * @return True if equal; false otherwise.
     */
    public static boolean operatorEquals(final Credential credential1, final Credential credential2)
    {
        if (credential1 == credential2)
            return true;
        if ((credential1 == null) || (null == credential2))
            return false;

        return credential1.Username.equals(credential2.Username)
                && credential1.Password.equals(credential2.Password);
    }
    /**
     * Compares two credentials for inequality.
     *
     * @param credential1 Credential to compare.
     * @param credential2 Credential to compare.
     * @return False if equal; true otherwise.
     */
    public static boolean operatorNotEquals(final Credential credential1, final Credential credential2)
    {
        return !operatorEquals(credential1, credential2);
    }
}
