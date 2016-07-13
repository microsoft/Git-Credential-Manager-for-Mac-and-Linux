// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.oauth2.useragent.AuthorizationException;
import com.microsoft.alm.secret.TokenPair;

import java.net.URI;

/**
 * An object that implements part of the OAuth 2.0 Device Flow.
 *
 * @see <a href="https://tools.ietf.org/html/draft-ietf-oauth-device-flow-01">
 *     OAuth 2.0 Device Flow IETF Draft 01
 *     </a>
 */
public interface DeviceFlow
{
    /**
     * The client initiates the flow by requesting a set of verification
     * codes from the authorization server's device endpoint.
     *
     * @param deviceEndpoint the authorization server's endpoint capable of issuing
     *                       verification codes, user codes, and verification URLs.
     * @param clientId       the client identifier as described in Section 2.2 of RFC6749.
     * @param scope          the scope of the access request as described by
     *                       Section 3.3 of RFC6749. (optional)
     * @return               a {@link DeviceFlowResponse} representing how the 2nd
     *                       phase of the protocol should proceed.
     *
     * @see                  "steps (A) and (B) of the Device Flow"
     */
    DeviceFlowResponse requestAuthorization(final URI deviceEndpoint, final String clientId, final String scope);

    /**
     * The client polls the authorization server's token endpoint repeatedly
     * until the end-user grants or denies the request, or the verification
     * code expires.
     *
     * @param tokenEndpoint           the authorization server's
     *                                token endpoint as described in Section 4.1.1 of RFC6749.
     * @param clientId                the client identifier as described in Section 2.2 of RFC6749.
     * @param deviceFlowResponse      the response obtained from
     *                                {@link #requestAuthorization(URI, String, String)}.
     * @return                        a pair of tokens.
     * @throws AuthorizationException the end-user denied the request,
     *                                or the verification code expired.
     *
     * @see                           "steps (E) and (F) of the Device Flow"
     */
    TokenPair requestToken(final URI tokenEndpoint, String clientId, final DeviceFlowResponse deviceFlowResponse) throws AuthorizationException;
}
