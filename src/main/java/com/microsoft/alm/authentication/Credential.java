// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.ObjectExtensions;
import com.microsoft.alm.helpers.StringHelper;
import com.microsoft.alm.helpers.XmlHelper;
import com.microsoft.alm.secret.Secret;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.bind.DatatypeConverter;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Credential for user authentication.
 */
public final class Credential extends Secret
{
    private static final Charset ASCII = Charset.forName("ASCII");

    public static final Credential Empty = new Credential(StringHelper.Empty, StringHelper.Empty);

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

    public static Credential fromXml(final Node credentialNode)
    {
        Credential value;
        String password = null;
        String username = null;

        final NodeList propertyNodes = credentialNode.getChildNodes();
        for (int v = 0; v < propertyNodes.getLength(); v++)
        {
            final Node propertyNode = propertyNodes.item(v);
            if (propertyNode.getNodeType() != Node.ELEMENT_NODE) continue;

            final String propertyName = propertyNode.getNodeName();
            if ("Password".equals(propertyName))
            {
                password = XmlHelper.getText(propertyNode);
            }
            else if ("Username".equals(propertyName))
            {
                username = XmlHelper.getText(propertyNode);
            }
        }
        value = new Credential(username, password);
        return value;
    }

    public Element toXml(final Document document)
    {
        final Element valueNode = document.createElement("value");

        final Element passwordNode = document.createElement("Password");
        final Text passwordValue = document.createTextNode(this.Password);
        passwordNode.appendChild(passwordValue);
        valueNode.appendChild(passwordNode);

        final Element usernameNode = document.createElement("Username");
        final Text usernameValue = document.createTextNode(this.Username);
        usernameNode.appendChild(usernameValue);
        valueNode.appendChild(usernameNode);

        return valueNode;
    }

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

    public void contributeHeader(final Map<String, String> headers)
    {
        // credentials are packed into the 'Authorization' header as a base64 encoded pair
        final String credPair = Username + ":" + Password;
        final byte[] credBytes = credPair.getBytes(ASCII);
        final String base64enc = DatatypeConverter.printBase64Binary(credBytes);
        headers.put("Authorization", "Basic" + " " + base64enc);

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
