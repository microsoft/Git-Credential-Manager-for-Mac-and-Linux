// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import java.net.URI;

public interface IVsoMsaAuthentication extends IAuthentication
{
    boolean interactiveLogon(final URI targetUri, boolean requestCompactToken);
    boolean refreshCredentials(final URI targetUri, final boolean requireCompactToken);
    boolean validateCredentials(final URI targetUri, final Credential credentials);
}
