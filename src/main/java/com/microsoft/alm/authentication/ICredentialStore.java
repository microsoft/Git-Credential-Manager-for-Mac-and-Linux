// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

public interface ICredentialStore
{
    void deleteCredentials(final URI targetUri);
    boolean readCredentials(final URI targetUri, final AtomicReference<Credential> credentials);
    void writeCredentials(final URI targetUri, final Credential credentials);
}
