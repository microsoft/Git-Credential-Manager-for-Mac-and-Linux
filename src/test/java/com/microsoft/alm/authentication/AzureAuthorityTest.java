// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.UUID;

public class AzureAuthorityTest
{

    @Test
    public void createAuthorizationEndpointUri_minimal() throws Exception
    {
        final URI redirectUri = URI.create("https://example.com");
        final URI actual = AzureAuthority.createAuthorizationEndpointUri(
                "https://login.microsoftonline.com/common",
                "a8860e8f-ca7d-4efe-b80d-4affab13d4ba", "f7e11bcd-b50b-4869-ad88-8bdd6cbc8473",
                redirectUri,
                UserIdentifier.ANY_USER,
                null,
                PromptBehavior.AUTO,
                null);

        Assert.assertEquals("https://login.microsoftonline.com/common/oauth2/authorize?resource=a8860e8f-ca7d-4efe-b80d-4affab13d4ba&client_id=f7e11bcd-b50b-4869-ad88-8bdd6cbc8473&response_type=code&redirect_uri=https%3A%2F%2Fexample.com", actual.toString());
    }

    @Test
    public void createAuthorizationEndpointUri_typical() throws Exception
    {
        final URI redirectUri = URI.create("https://example.com");
        final UUID correlationId = UUID.fromString("519a4fa6-c18f-4230-8290-6c57407656c9");
        final URI actual = AzureAuthority.createAuthorizationEndpointUri(
                "https://login.microsoftonline.com/common",
                "a8860e8f-ca7d-4efe-b80d-4affab13d4ba", "f7e11bcd-b50b-4869-ad88-8bdd6cbc8473",
                redirectUri,
                UserIdentifier.ANY_USER,
                correlationId,
                PromptBehavior.ALWAYS,
                null);

        Assert.assertEquals("https://login.microsoftonline.com/common/oauth2/authorize?resource=a8860e8f-ca7d-4efe-b80d-4affab13d4ba&client_id=f7e11bcd-b50b-4869-ad88-8bdd6cbc8473&response_type=code&redirect_uri=https%3A%2F%2Fexample.com&client-request-id=519a4fa6-c18f-4230-8290-6c57407656c9&prompt=login", actual.toString());
    }

    @Test
    public void createAuthorizationEndpointUri_extraState() throws Exception
    {
        final URI redirectUri = URI.create("https://example.com");
        final UUID correlationId = UUID.fromString("519a4fa6-c18f-4230-8290-6c57407656c9");
        final URI actual = AzureAuthority.createAuthorizationEndpointUri(
                "https://login.microsoftonline.com/common",
                "a8860e8f-ca7d-4efe-b80d-4affab13d4ba", "f7e11bcd-b50b-4869-ad88-8bdd6cbc8473",
                redirectUri,
                UserIdentifier.ANY_USER,
                correlationId,
                PromptBehavior.ALWAYS,
                "state=bliss");

        Assert.assertEquals("https://login.microsoftonline.com/common/oauth2/authorize?resource=a8860e8f-ca7d-4efe-b80d-4affab13d4ba&client_id=f7e11bcd-b50b-4869-ad88-8bdd6cbc8473&response_type=code&redirect_uri=https%3A%2F%2Fexample.com&client-request-id=519a4fa6-c18f-4230-8290-6c57407656c9&prompt=login&state=bliss", actual.toString());
    }

}
