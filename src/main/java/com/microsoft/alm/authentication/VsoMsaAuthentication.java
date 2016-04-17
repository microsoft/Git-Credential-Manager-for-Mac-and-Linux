// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Action;
import com.microsoft.alm.helpers.Trace;

import java.net.URI;

public final class VsoMsaAuthentication extends BaseVsoAuthentication implements IVsoMsaAuthentication
{
    public final String DefaultAuthorityHost = AzureAuthority.AuthorityHostUrlBase + "/live.com";

    public VsoMsaAuthentication(
            VsoTokenScope tokenScope,
            ICredentialStore personalAccessTokenStore,
            ITokenStore adaRefreshTokenStore)
    {
        super(tokenScope,
               personalAccessTokenStore,
               adaRefreshTokenStore);
        this.VsoAuthority = new VsoAzureAuthority(DefaultAuthorityHost);
    }
    /**
     * Test constructor which allows for using fake credential stores
     */
    VsoMsaAuthentication(
            ICredentialStore personalAccessTokenStore,
            ITokenStore adaRefreshTokenStore,
            ITokenStore vsoIdeTokenCache,
            IVsoAuthority liveAuthority)
    {
        super(personalAccessTokenStore,
               adaRefreshTokenStore,
               vsoIdeTokenCache,
               liveAuthority);
    }

    /**
     * Opens an interactive logon prompt to acquire acquire an authentication token from the
     * Microsoft Live authentication and identity service.
     *
     * @param targetUri
     * The uniform resource indicator of the resource access tokens are being requested for.
     * 
     * @param requireCompactToken
     * True if a compact access token is required; false if a standard token is acceptable.
     * 
     * @return True if successful; otherwise false.
     */
    @Override public boolean interactiveLogon(URI targetUri, boolean requireCompactToken)
    {
        final String QueryParameters = "domain_hint=live.com&display=popup&site_id=501454&nux=1";

        BaseSecureStore.validateTargetUri(targetUri);

        Trace.writeLine("VsoMsaAuthentication::InteractiveLogon");

        TokenPair tokens;
        if ((tokens = this.VsoAuthority.acquireToken(targetUri, this.ClientId, this.Resource, RedirectUri, QueryParameters)) != null)
        {
            Trace.writeLine("   token successfully acquired.");

            this.storeRefreshToken(targetUri, tokens.RefreshToken);

            return this.generatePersonalAccessToken(targetUri, tokens.AccessToken, requireCompactToken);
        }

        Trace.writeLine("   failed to acquire token.");
        return false;
    }

    public boolean deviceLogon(final URI targetUri, final boolean requestCompactToken, final Action<DeviceFlowResponse> callback)
    {
        BaseSecureStore.validateTargetUri(targetUri);

        Trace.writeLine("VsoMsaAuthentication::deviceLogon");

        TokenPair tokens;
        if ((tokens = this.VsoAuthority.acquireToken(targetUri, this.ClientId, this.Resource, callback)) != null)
        {
            Trace.writeLine("   token successfully acquired.");

            this.storeRefreshToken(targetUri, tokens.RefreshToken);

            return this.generatePersonalAccessToken(targetUri, tokens.AccessToken, requestCompactToken);
        }

        Trace.writeLine("   failed to acquire token.");
        return false;
    }

    /**
     * Sets credentials for future use with this authentication object.
     * Not supported.
     * @param targetUri The uniform resource indicator of the resource access tokens are being set for.
     * 
     * @param credentials The credentials being set.
     * @return True if successful; false otherwise.
     */
    @Override public boolean setCredentials(URI targetUri, Credential credentials)
    {
        BaseSecureStore.validateTargetUri(targetUri);
        Credential.validate(credentials);

        Trace.writeLine("VsoMsaAuthentication::SetCredentials");
        Trace.writeLine("   setting MSA credentials is not supported");

        // does nothing with VSO MSA backed accounts
        return false;
    }

}
