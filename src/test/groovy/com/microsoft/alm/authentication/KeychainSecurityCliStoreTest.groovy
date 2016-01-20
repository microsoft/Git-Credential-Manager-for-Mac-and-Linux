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

    @Test public void parseAttributeLine_stringKeyBlobString() {
        def input = '''    "acct"<blob>="chuck.norris"'''
        def destination = [:]

        KeychainSecurityCliStore.parseAttributeLine(input, destination)

        assert ["acct" : "chuck.norris"] == destination
    }

    @Test public void parseAttributeLine_stringKeyBlobStringContainsDoubleQuote() {
        def input = '''    "desc"<blob>="A string with "double quotes" inside"'''
        def destination = [:]

        KeychainSecurityCliStore.parseAttributeLine(input, destination)

        assert ["desc" : 'A string with "double quotes" inside'] == destination
    }

    @Test public void parseAttributeLine_stringKeyBlobNull() {
        def input = '''    "acct"<blob>=<NULL>'''
        def destination = [:]

        KeychainSecurityCliStore.parseAttributeLine(input, destination)

        assert ["acct" : null] == destination
    }

    @Test public void parseAttributeLine_hexKeyBlobString() {
        def input = '''    0x00000007 <blob>="gcm4ml:git:https://example.visualstudio.com"'''
        def destination = [:]

        KeychainSecurityCliStore.parseAttributeLine(input, destination)

        assert ["0x00000007" : "gcm4ml:git:https://example.visualstudio.com"] == destination
    }

    @Test public void parseAttributeLine_hexKeyBlobNull() {
        def input = '''    0x00000008 <blob>=<NULL>'''
        def destination = [:]

        KeychainSecurityCliStore.parseAttributeLine(input, destination)

        assert ["0x00000008" : null] == destination
    }
}
