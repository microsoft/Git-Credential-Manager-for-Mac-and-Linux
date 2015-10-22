// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

class PromptValue
{
    static final String LOGIN = "login";
    static final String REFRESH_SESSION = "refresh_session";

    // The behavior of this value is identical to prompt=none for managed users; However, for federated users, AAD
    // redirects to ADFS as it cannot determine in advance whether ADFS can login user silently (e.g. via WIA) or not.
    static final String ATTEMPT_NONE = "attempt_none";
}
