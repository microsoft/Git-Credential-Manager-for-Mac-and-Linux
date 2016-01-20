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

    @Test public void parseKeychainMetaData_typical() {
        def input = """\
keychain: "/Users/chuck.norris/Library/Keychains/login.keychain"
class: "genp"
attributes:
    0x00000007 <blob>="gcm4ml:git:https://example.visualstudio.com"
    0x00000008 <blob>=<NULL>
    "acct"<blob>="chuck.norris"
    "cdat"<timedate>=0x32303135313030353139343332355A00  "20151005194325Z\\000"
    "crtr"<uint32>="aapl"
    "cusi"<sint32>=<NULL>
    "desc"<blob>=<NULL>
    "gena"<blob>=<NULL>
    "icmt"<blob>=<NULL>
    "invi"<sint32>=<NULL>
    "mdat"<timedate>=0x32303135313030353139343332355A00  "20151005194325Z\\000"
    "nega"<sint32>=<NULL>
    "prot"<blob>=<NULL>
    "scrp"<sint32>=<NULL>
    "svce"<blob>="gcm4ml:git:https://example.visualstudio.com"
    "type"<uint32>=<NULL>
"""

        def actual = KeychainSecurityCliStore.parseKeychainMetaData(input)

        def expected = [
            "keychain": "/Users/chuck.norris/Library/Keychains/login.keychain",
            "class": "genp",
            "0x00000007": "gcm4ml:git:https://example.visualstudio.com",
            "0x00000008": null,
            "acct": "chuck.norris",
            // Not supported: "cdat" : '0x32303135313030353139343332355A00  "20151005194325Z\\000"',
            // Not supported: "crtr" : "aapl",
            "cusi" : null,
            "desc" : null,
            "gena" : null,
            "icmt" : null,
            "invi" : null,
            // Not supported: "mdat" : '0x32303135313030353139343332355A00  "20151005194325Z\\000"',
            "nega" : null,
            "prot" : null,
            "scrp" : null,
            "svce" : "gcm4ml:git:https://example.visualstudio.com",
            "type" : null,
        ]
        assert expected == actual
    }

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
