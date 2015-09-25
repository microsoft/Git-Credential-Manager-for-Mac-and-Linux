package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Debug;
import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.helpers.StringHelper;

class TokenPair // TODO: implements IEquatable<TokenPair>
{
    /**
     * Creates a new {@link TokenPair} from raw access and refresh token data.
     *
     * @param accessToken  The base64 encoded value of the access token's raw data
     * @param refreshToken The base64 encoded value of the refresh token's raw data
     */
    public TokenPair(final String accessToken, final String refreshToken)
    {
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(accessToken), "The accessToken parameter is null or invalid.");
        Debug.Assert(!StringHelper.isNullOrWhiteSpace(refreshToken), "The refreshToken parameter is null or invalid.");

        this.AccessToken = new Token(accessToken, TokenType.Access);
        this.RefreshToken = new Token(refreshToken, TokenType.Refresh);
    }

    /**
     * Creates a new {@link TokenPair} from an ADAL {@literal AuthenticationResult}.
     *
     * @param authResult A successful AuthenticationResult
     *                   which contains both access and refresh token data.
     */
    public TokenPair(final Object /* TODO: AD.AuthenticationResult */ authResult)
    {
        throw new NotImplementedException();
    }

    /**
     * Access token, used to grant access to resources.
     */
    public final Token AccessToken;
    /**
     * Refresh token, used to grant new access tokens.
     */
    public final Token RefreshToken;

    /**
     * Compares an object to this.
     *
     * @param object The object to compare.
     * @return True if equal; false otherwise
     */
    @Override public boolean equals(final Object object)
    {
        return operatorEquals(this, object instanceof TokenPair ? ((TokenPair) object) : null);
    }
    // PORT NOTE: Java doesn't support a specific overload (as per IEquatable<T>)
    /**
     * Gets a hash code based on the contents of the {@link TokenPair}.
     *
     * @return 32-bit hash code.
     */
    @Override public int hashCode()
    {
        // PORT NOTE: Java doesn't have unchecked blocks; the default behaviour is apparently equivalent.
        {
            return AccessToken.hashCode() * RefreshToken.hashCode();
        }
    }

    /**
     * Compares two {@link TokenPair} for equality.
     *
     * @param pair1 {@link TokenPair} to compare.
     * @param pair2 {@link TokenPair} to compare.
     * @return True if equal; false otherwise.
     */
    public static boolean operatorEquals(final TokenPair pair1, final TokenPair pair2)
    {
        if (pair1 == pair2)
            return true;
        if ((pair1 == null) || (null == pair2))
            return false;

        return Token.operatorEquals(pair1.AccessToken, pair2.AccessToken)
            && Token.operatorEquals(pair1.RefreshToken, pair2.RefreshToken);
    }

    /**
     * Compares two {@link TokenPair} for inequality.
     *
     * @param pair1 {@link TokenPair} to compare.
     * @param pair2 {@link TokenPair} to compare.
     * @return False if equal; true otherwise.
     */
    public static boolean operatorNotEquals(final TokenPair pair1, final TokenPair pair2)
    {
        return !operatorEquals(pair1, pair2);
    }
}
