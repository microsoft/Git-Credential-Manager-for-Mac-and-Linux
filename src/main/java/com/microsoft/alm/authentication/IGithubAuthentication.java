// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.secret.Credential;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public interface IGithubAuthentication extends IAuthentication
{
    boolean interactiveLogon(final URI targetUri, final AtomicReference<Credential> credentials);
    boolean noninteractiveLogonWithCredentials(final URI targetUri, final String username, final String password, final String authenticationCode);
    boolean validateCredentials(final URI targetUri, final Credential credentials);
}
