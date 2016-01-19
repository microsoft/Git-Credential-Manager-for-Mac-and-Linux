// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication

import groovy.transform.CompileStatic
import org.junit.Test;

/**
 * A class to test {@see KeychainSecurityCliStore}.
 */
@CompileStatic
public class KeychainSecurityCliStoreTest {

    @Test public void parseMetadataLine() {
        def input = '''keychain: "/Users/chuck.norris/Library/Keychains/login.keychain"'''
        def destination = [:]

        KeychainSecurityCliStore.parseMetadataLine(input, destination)

        assert ["keychain" : "/Users/chuck.norris/Library/Keychains/login.keychain"] == destination
    }
}
