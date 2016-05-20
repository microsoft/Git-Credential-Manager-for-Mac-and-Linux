// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import com.microsoft.alm.oauth2.useragent.AuthorizationException;
import com.microsoft.alm.oauth2.useragent.AuthorizationResponse;
import com.microsoft.alm.oauth2.useragent.UserAgent;

import java.net.URI;

/**
 * An implementation of {@link UserAgent} that does nothing.
 */
public class NullUserAgent implements UserAgent {

    public static final UserAgent INSTANCE = new NullUserAgent();

    private NullUserAgent() {
        // not meant to be constructed by others
    }

    @Override
    public AuthorizationResponse requestAuthorizationCode(
            final URI authorizationEndpoint, final URI redirectUri) throws AuthorizationException {
        return null;
    }
}
