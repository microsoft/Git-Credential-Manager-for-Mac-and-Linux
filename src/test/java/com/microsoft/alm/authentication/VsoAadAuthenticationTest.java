// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import com.microsoft.alm.helpers.Guid;
import com.microsoft.alm.secret.VsoTokenScope;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

public class VsoAadAuthenticationTest
{
    @Test public void ctor_DefaultAuthorityHost() throws URISyntaxException
    {
        final SecretCache secretCache = new SecretCache("test");

        final VsoAadAuthentication vaa = new VsoAadAuthentication(Guid.Empty, VsoTokenScope.CodeWrite, secretCache, secretCache);

        final AzureAuthority azureAuthority = (AzureAuthority) vaa.VsoAuthority;
        final URI uri = new URI(azureAuthority.authorityHostUrl);
        Assert.assertEquals(true, uri.isAbsolute());
    }
}
