// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

class OAuthParameter
{
    static final String RESPONSE_TYPE = "response_type";
    static final String GRANT_TYPE = "grant_type";
    static final String AUTHORIZATION_CODE = "authorization_code";
    static final String DEVICE_CODE = "device_code";
    static final String USER_CODE = "user_code";
    static final String CLIENT_ID = "client_id";
    static final String REDIRECT_URI = "redirect_uri";
    static final String VERIFICATION_URI = "verification_uri";
    static final String RESOURCE = "resource";
    static final String SCOPE = "scope";
    static final String CODE = "code";
    static final String EXPIRES_IN = "expires_in";
    static final String LOGIN_HINT = "login_hint";
    static final String STATE = "state";
    static final String INTERVAL = "interval";

    static final String CORRELATION_ID = "client-request-id"; // correlation id is not standard oauth2 parameter
    static final String REQUEST_CORRELATION_ID_IN_RESPONSE = "return-client-request-id"; // not standard oauth2 parameter
    static final String PROMPT = "prompt"; // prompt is not standard oauth2 parameter
}
