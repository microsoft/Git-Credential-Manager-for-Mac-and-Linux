package com.microsoft.alm.java.git_credential_helper.authentication;

import com.microsoft.alm.java.git_credential_helper.helpers.NotImplementedException;

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
        throw new NotImplementedException();
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
        throw new NotImplementedException();
    }

    /**
     * Gets a hash code based on the contents of the {@link TokenPair}.
     *
     * @return 32-bit hash code.
     */
    @Override public int hashCode()
    {
        throw new NotImplementedException();
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
        throw new NotImplementedException();
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
        throw new NotImplementedException();
    }
}
