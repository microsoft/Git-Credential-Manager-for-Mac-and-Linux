package com.microsoft.alm.java.git_credential_helper.authentication;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public interface ITokenStore
{
    /**
     * Deletes a {@link Token} from the underlying storage.
     *
     * @param targetUri The key identifying which token is being deleted.
     */
    void deleteToken(final URI targetUri);

    /**
     * Reads a {@link Token} from the underlying storage.
     *
     * @param targetUri The key identifying which token to read.
     * @param token A {@link Token} if successful; otherwise null.
     * @return True if successful; otherwise false.
     */
    boolean readToken(final URI targetUri, final AtomicReference<Token> token);

    /**
     * Writes a {@link Token} to the underlying storage.
     *
     * @param targetUri Unique identifier for the token, used when reading back from storage.
     * @param token The {@link Token} to be written.
     */
    void writeToken(final URI targetUri, final Token token);
}
