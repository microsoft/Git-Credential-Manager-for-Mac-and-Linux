// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

/**
 * Indicates whether acquireToken should automatically prompt only if necessary or whether
 * it should prompt regardless of whether there is a cached token.
 */
public enum PromptBehavior {
    /**
     * Acquire token will prompt the user for credentials only when necessary.  If a token
     * that meets the requirements is already cached then the user will not be prompted.
     */
    AUTO,

    /**
     * The user will be prompted for credentials even if there is a token that meets the requirements
     * already in the cache.
     */
    ALWAYS,

    /**
     * The user will not be prompted for credentials.  If prompting is necessary then the acquireToken request
     * will fail.
     */
    NEVER,

    /**
     * Not yet implemented. Reserved for future use.
     */
    /*
     * Re-authorizes (through displaying browser) the resource usage, making sure that the resulting access
     * token contains updated claims. If user logon cookies are available, the user will not be asked for
     * credentials again and the logon dialog will dismiss automatically.
     */
    REFRESH_SESSION,
    ;
}
