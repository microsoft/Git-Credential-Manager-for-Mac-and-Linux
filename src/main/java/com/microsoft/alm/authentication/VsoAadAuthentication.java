// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Action;
import com.microsoft.alm.helpers.Guid;
import com.microsoft.alm.helpers.NotImplementedException;
import com.microsoft.alm.helpers.Trace;

import java.net.URI;
import java.util.UUID;

/**
 * Facilitates Azure Directory authentication.
 */
public final class VsoAadAuthentication extends BaseVsoAuthentication implements IVsoAadAuthentication
{
    /**
     * The default authority host for all Azure Directory authentication
     */
    public static final String DefaultAuthorityHost = "https://management.core.windows.net/";

    /**
     * @param tenantId                 <p>The unique identifier for the responsible Azure tenant.</p>
     *                                 <p>Use {@link BaseVsoAuthentication}
     *                                 to detect the tenant identity and create the authentication object.</p>
     * @param tokenScope               The scope of all access tokens acquired by the authority.
     * @param personalAccessTokenStore The secure secret store for storing any personal
     *                                 access tokens acquired.
     * @param adaRefreshTokenStore     The secure secret store for storing any Azure tokens
     *                                 acquired
     */
    public VsoAadAuthentication(
            final UUID tenantId,
            final VsoTokenScope tokenScope,
            final ICredentialStore personalAccessTokenStore,
            final ITokenStore adaRefreshTokenStore)
    {
        super(tokenScope,
              personalAccessTokenStore,
              adaRefreshTokenStore);
        if (tenantId == null || tenantId.equals(Guid.Empty))
        {
            this.VsoAuthority = new VsoAzureAuthority(DefaultAuthorityHost);
        }
        else
        {
            // create an authority host url in the format of https://login.microsoft.com/12345678-9ABC-DEF0-1234-56789ABCDEF0
            String authorityHost = AzureAuthority.getAuthorityUrl(tenantId);
            this.VsoAuthority = new VsoAzureAuthority(authorityHost);
        }
    }

    /**
     * Test constructor which allows for using fake credential stores
     */
    VsoAadAuthentication(
            final ICredentialStore personalAccessTokenStore,
            final ITokenStore adaRefreshTokenStore,
            final ITokenStore vsoIdeTokenCache,
            final IVsoAuthority vsoAuthority)
    {
        super(personalAccessTokenStore,
              adaRefreshTokenStore,
              vsoIdeTokenCache,
              vsoAuthority);
    }

    /**
     * <p>Creates an interactive logon session, using ADAL secure browser GUI, which
     * enables users to authenticate with the Azure tenant and acquire the necessary access
     * tokens to exchange for a VSO personal access token.</p>
     * <p>Tokens acquired are stored in the secure secret stores provided during
     * initialization.</p>
     *
     * @param targetUri           The unique identifier for the resource for which access is to
     *                            be acquired.
     * @param requestCompactToken <p>Requests a compact format personal access token; otherwise requests a standard
     *                            personal access token.</p>
     *                            <p>Compact tokens are necessary for clients which have restrictions on the size of
     *                            the basic authentication header which they can create (example: Git).</p>
     * @return                    True if a authentication and personal access token acquisition was successful; otherwise false.
     */
    public boolean interactiveLogon(final URI targetUri, final boolean requestCompactToken)
    {
        BaseSecureStore.validateTargetUri(targetUri);

        Trace.writeLine("VsoAadAuthentication::interactiveLogon");

        TokenPair tokens;
        if ((tokens = this.VsoAuthority.acquireToken(targetUri, this.ClientId, this.Resource, RedirectUri, null)) != null)
        {
            Trace.writeLine("   token acquisition succeeded.");

            this.storeRefreshToken(targetUri, tokens.RefreshToken);

            return this.generatePersonalAccessToken(targetUri, tokens.AccessToken,  requestCompactToken);
        }

        Trace.writeLine("   interactive logon failed");
        return false;
    }

    /**
     * <p>Uses credentials to authenticate with the Azure tenant and acquire the necessary
     * access tokens to exchange for a VSO personal access token.</p>
     * <p>Tokens acquired are stored in the secure secret stores provided during
     * initialization.</p>
     *
     * @param targetUri           The unique identifier for the resource for which access is to
     *                            be acquired.
     * @param credentials         The credentials required to meet the criteria of the Azure
     *                            tenant authentication challenge (i.e. username + password).
     * @param requestCompactToken <p>Requests a compact format personal access token; otherwise requests a standard
     *                            personal access token.</p>
     *                            <p>Compact tokens are necessary for clients which have restrictions on the size of
     *                            the basic authentication header which they can create (example: Git).</p>
     * @return                    True if authentication and personal access token acquisition was successful; otherwise false.
     */
    public boolean noninteractiveLogonWithCredentials(final URI targetUri, final Credential credentials, final boolean requestCompactToken)
    {
        throw new NotImplementedException(449288);
    }

    /**
     * <p>Uses Active Directory Federation Services to authenticate with the Azure tenant
     * non-interactively and acquire the necessary access tokens to exchange for a VSO personal
     * access token.</p>
     * <p>Tokens acquired are stored in the secure secret stores provided during
     * initialization.</p>
     *
     * @param targetUri           The unique identifier for the resource for which access is to
     *                            be acquired.
     * @param requestCompactToken <p>Requests a compact format personal access token; otherwise requests a standard
     *                            personal access token.</p>
     *                            <p>Compact tokens are necessary for clients which have restrictions on the size of
     *                            the basic authentication header which they can create (example: Git).</p>
     * @return                    True if authentication and personal access token acquisition was successful; otherwise false.
     */
    public boolean noninteractiveLogon(final URI targetUri, final boolean requestCompactToken)
    {
        throw new NotImplementedException(449285);
    }

    public boolean deviceLogon(final URI targetUri, final boolean requestCompactToken, final Action<DeviceFlowResponse> callback)
    {
        throw new NotImplementedException(560199);
    }

    /**
     * Sets credentials for future use with this authentication object.
     *
     * Not supported.
     * @param targetUri   The uniform resource indicator of the resource access tokens are being set for.
     * @param credentials The credentials being set.
     * @return            True if successful; false otherwise.
     */
    @Override public boolean setCredentials(final URI targetUri, final Credential credentials)
    {
        BaseSecureStore.validateTargetUri(targetUri);
        Credential.validate(credentials);

        Trace.writeLine("VsoMsaAuthentication::SetCredentials");
        Trace.writeLine("   setting AAD credentials is not supported");

        // does nothing with VSO AAD backed accounts
        return false;

    }
}
