// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication;

import groovy.transform.CompileStatic
import org.junit.Test

import static org.junit.Assert.assertEquals

/**
 * A class to test {@see DeviceFlowResponse}.
 */
@CompileStatic
public class VsoAzureAuthorityTest {

    @Test
    public void parseLocationServiceUriFromJson_success() throws Exception {
        final json = """
        {
            "serviceType": "LocationService2",
            "identifier": "852017ac-a960-5000-8464-e3f0aa25b381",
            "displayName": "SPS Location Service",
            "relativeToSetting": "fullyQualified",
            "description": "",
            "serviceOwner": "00000000-0000-0000-0000-000000000000",
            "locationMappings": [{
                "accessMappingMoniker": "HostGuidAccessMapping",
                "location": "https://app.vssps.visualstudio.com/Ca576a7c5-ab56-424e-91ba-66c86e0fff0d/"
            }],
            "toolId": "Framework",
            "parentServiceType": "LocationService2",
            "parentIdentifier": "b6cd35eb-148e-4aad-bbb3-d31576d75958",
            "properties": {}
        }
        """

        final URI locationService = VsoAzureAuthority.parseLocationFromJson(json);
        assertEquals("https://app.vssps.visualstudio.com/Ca576a7c5-ab56-424e-91ba-66c86e0fff0d/", locationService.toString());
    }

    @Test
    public void parseLocationServiceFromAppVsspsJson_success() throws Exception {
        final json = """
        {
            "locationMappings": [{
                "accessMappingMoniker": "HostGuidAccessMapping",
                "location": "https://app.vssps.visualstudio.com/"
            }],
        }
        """

        final URI locationService = VsoAzureAuthority.parseLocationFromJson(json);
        assertEquals("https://app.vssps.visualstudio.com/", locationService.toString());
    }

    @Test
    public void parseLocationServiceFromAppVsspsNoSlashJson_success() throws Exception {
        final json = """
        {
            "locationMappings": [{
                "accessMappingMoniker": "HostGuidAccessMapping",
                "location": "https://app.vssps.visualstudio.com"
            }]
        }
        """

        final URI locationService = VsoAzureAuthority.parseLocationFromJson(json);
        assertEquals("https://app.vssps.visualstudio.com", locationService.toString());
    }
}