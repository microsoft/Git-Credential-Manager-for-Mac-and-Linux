// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication

import com.microsoft.alm.helpers.StringHelper
import com.microsoft.alm.oauth2.useragent.subprocess.DefaultProcessFactory
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcessFactory
import groovy.transform.CompileStatic
import org.junit.Ignore
import org.junit.Test;

/**
 * A class to test {@see KeychainSecurityCliStore}.
 */
@CompileStatic
public class KeychainSecurityCliStoreTest {

    static final String USER_NAME = "chuck.norris"
    static final String PASSWORD = "roundhouse"
    static final String PASSWORD2 = "roundhouse kick"
    static final String VSTS_ACCOUNT = "https://example.visualstudio.com"
    static final String TARGET_NAME = "git:${VSTS_ACCOUNT}"
    static final String SERVICE_NAME = "gcm4ml:${TARGET_NAME}"
    static final String SAMPLE_CREDENTIAL_METADATA = """\
keychain: "/Users/${USER_NAME}/Library/Keychains/login.keychain"
class: "genp"
attributes:
    0x00000007 <blob>="${SERVICE_NAME}"
    0x00000008 <blob>=<NULL>
    "acct"<blob>="${USER_NAME}"
    "cdat"<timedate>=0x32303135313030353139343332355A00  "20151005194325Z\\000"
    "crtr"<uint32>="aapl"
    "cusi"<sint32>=<NULL>
    "desc"<blob>="Credential"
    "gena"<blob>=<NULL>
    "icmt"<blob>=<NULL>
    "invi"<sint32>=<NULL>
    "mdat"<timedate>=0x32303135313030353139343332355A00  "20151005194325Z\\000"
    "nega"<sint32>=<NULL>
    "prot"<blob>=<NULL>
    "scrp"<sint32>=<NULL>
    "svce"<blob>="${SERVICE_NAME}"
    "type"<uint32>=<NULL>
"""

    static final String SAMPLE_TOKEN_METADATA = """\
keychain: "/Users/${USER_NAME}/Library/Keychains/login.keychain"
class: "genp"
attributes:
    0x00000007 <blob>="${SERVICE_NAME}"
    0x00000008 <blob>=<NULL>
    "acct"<blob>="Personal Access Token"
    "cdat"<timedate>=0x32303135313030353139343332355A00  "20151005194325Z\\000"
    "crtr"<uint32>="aapl"
    "cusi"<sint32>=<NULL>
    "desc"<blob>="Token"
    "gena"<blob>=<NULL>
    "icmt"<blob>=<NULL>
    "invi"<sint32>=<NULL>
    "mdat"<timedate>=0x32303135313030353139343332355A00  "20151005194325Z\\000"
    "nega"<sint32>=<NULL>
    "prot"<blob>=<NULL>
    "scrp"<sint32>=<NULL>
    "svce"<blob>="${SERVICE_NAME}"
    "type"<uint32>=<NULL>
"""

    @Test public void parseKeychainMetaData_typical() {

        def actual = KeychainSecurityCliStore.parseKeychainMetaData(SAMPLE_CREDENTIAL_METADATA)

        def expected = [
            "keychain": "/Users/${USER_NAME}/Library/Keychains/login.keychain",
            "class": "genp",
            "0x00000007": SERVICE_NAME,
            "0x00000008": null,
            "acct": USER_NAME,
            // Not supported: "cdat" : '0x32303135313030353139343332355A00  "20151005194325Z\\000"',
            // Not supported: "crtr" : "aapl",
            "cusi" : null,
            "desc" : "Credential",
            "gena" : null,
            "icmt" : null,
            "invi" : null,
            // Not supported: "mdat" : '0x32303135313030353139343332355A00  "20151005194325Z\\000"',
            "nega" : null,
            "prot" : null,
            "scrp" : null,
            "svce" : SERVICE_NAME,
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

    @Test public void parseMetadataLine_containsDoubleQuote() {
        def input = '''password: "A string with "double quotes" inside"'''
        def destination = [:]

        KeychainSecurityCliStore.parseMetadataLine(input, destination)

        assert ["password" : 'A string with "double quotes" inside'] == destination
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

    @Test public void simulatedProbing_keychainIsAvailable() {
        def addToken = new FifoProcess(StringHelper.Empty)
        addToken.with {
            expectedCommand = ["/usr/bin/security", "add-generic-password", "-U", "-a", "Test-only Token", "-s", "gcm4ml:test:isKeychainAvailable", "-w", "this is a test token", "-D", "Token"]
            expectedExitCode = 0
        }

        def deleteTokenSuccess = new FifoProcess(SAMPLE_TOKEN_METADATA, "password has been deleted.")
        deleteTokenSuccess.with {
            expectedCommand = ["/usr/bin/security", "delete-generic-password", "-s", "gcm4ml:test:isKeychainAvailable"]
            expectedExitCode = 0
        }

        def processFactory = new FifoProcessFactory(
            addToken,
            deleteTokenSuccess,
        )
        probingTest(processFactory, true)
    }

    @Test public void simulatedProbing_keychainIsNotAvailable() {
        def addToken = new FifoProcess(StringHelper.Empty)
        addToken.with {
            expectedCommand = ["/usr/bin/security", "add-generic-password", "-U", "-a", "Test-only Token", "-s", "gcm4ml:test:isKeychainAvailable", "-w", "this is a test token", "-D", "Token"]
            expectedExitCode = 36
        }

        def processFactory = new FifoProcessFactory(
                addToken,
        )
        probingTest(processFactory, false)
    }

    @Ignore("Needs to be run manually, in interactive mode, because the Keychain needs a desktop")
    @Test public void interactiveProbing() {
        def processFactory = new DefaultProcessFactory()
        probingTest(processFactory, true)
    }

    static void probingTest(final TestableProcessFactory processFactory, final boolean expected) {
        final def store = new KeychainSecurityCliStore(processFactory)

        final def actual = store.isKeychainAvailable()

        assert actual == expected
    }

    @Test public void simulatedInteraction() {
        def deleteCredentialSuccess = new FifoProcess(SAMPLE_CREDENTIAL_METADATA, "password has been deleted.")
        deleteCredentialSuccess.with {
            expectedCommand = ["/usr/bin/security", "delete-generic-password", "-s", SERVICE_NAME]
        }

        def deleteCredentialFailure = new FifoProcess(StringHelper.Empty, "security: SecKeychainSearchCopyNext: The specified item could not be found in the keychain.")
        deleteCredentialFailure.with {
            expectedCommand = ["/usr/bin/security", "delete-generic-password", "-s", SERVICE_NAME]
            expectedExitCode = 44
        }

        def findNoCredential = new FifoProcess(StringHelper.Empty, "security: SecKeychainSearchCopyNext: The specified item could not be found in the keychain.")
        findNoCredential.with {
            expectedCommand = ["/usr/bin/security", "find-generic-password", "-s", SERVICE_NAME, "-D", "Credential", "-g"]
            expectedExitCode = 44
        }

        def addCredential = new FifoProcess(StringHelper.Empty)
        addCredential.with {
            expectedCommand = ["/usr/bin/security", "add-generic-password", "-U", "-a", USER_NAME, "-s", SERVICE_NAME, "-w", PASSWORD, "-D", "Credential"]
            expectedExitCode = 0
        }

        def findCredential = new FifoProcess(SAMPLE_CREDENTIAL_METADATA, """password: "${PASSWORD}"
""")
        findCredential.with {
            expectedCommand = ["/usr/bin/security", "find-generic-password", "-s", SERVICE_NAME, "-D", "Credential", "-g"]
            expectedExitCode = 0
        }

        def updateCredential = new FifoProcess(StringHelper.Empty)
        updateCredential.with {
            expectedCommand = ["/usr/bin/security", "add-generic-password", "-U", "-a", USER_NAME, "-s", SERVICE_NAME, "-w", PASSWORD2, "-D", "Credential"]
            expectedExitCode = 0
        }

        def findUpdatedCredential = new FifoProcess(SAMPLE_CREDENTIAL_METADATA, """password: "${PASSWORD2}"
""")
        findUpdatedCredential.with {
            expectedCommand = ["/usr/bin/security", "find-generic-password", "-s", SERVICE_NAME, "-D", "Credential", "-g"]
            expectedExitCode = 0
        }

        
        def deleteTokenSuccess = new FifoProcess(SAMPLE_TOKEN_METADATA, "password has been deleted.")
        deleteTokenSuccess.with {
            expectedCommand = ["/usr/bin/security", "delete-generic-password", "-s", SERVICE_NAME]
        }

        def deleteTokenFailure = new FifoProcess(StringHelper.Empty, "security: SecKeychainSearchCopyNext: The specified item could not be found in the keychain.")
        deleteTokenFailure.with {
            expectedCommand = ["/usr/bin/security", "delete-generic-password", "-s", SERVICE_NAME]
            expectedExitCode = 44
        }

        def findNoToken = new FifoProcess(StringHelper.Empty, "security: SecKeychainSearchCopyNext: The specified item could not be found in the keychain.")
        findNoToken.with {
            expectedCommand = ["/usr/bin/security", "find-generic-password", "-s", SERVICE_NAME, "-D", "Token", "-g"]
            expectedExitCode = 44
        }

        def addToken = new FifoProcess(StringHelper.Empty)
        addToken.with {
            expectedCommand = ["/usr/bin/security", "add-generic-password", "-U", "-a", "Personal Access Token", "-s", SERVICE_NAME, "-w", PASSWORD, "-D", "Token"]
            expectedExitCode = 0
        }

        def findToken = new FifoProcess(SAMPLE_TOKEN_METADATA, """password: "${PASSWORD}"
""")
        findToken.with {
            expectedCommand = ["/usr/bin/security", "find-generic-password", "-s", SERVICE_NAME, "-D", "Token", "-g"]
            expectedExitCode = 0
        }

        def updateToken = new FifoProcess(StringHelper.Empty)
        updateToken.with {
            expectedCommand = ["/usr/bin/security", "add-generic-password", "-U", "-a", "Personal Access Token", "-s", SERVICE_NAME, "-w", PASSWORD2, "-D", "Token"]
            expectedExitCode = 0
        }

        def findUpdatedToken = new FifoProcess(SAMPLE_TOKEN_METADATA, """password: "${PASSWORD2}"
""")
        findUpdatedToken.with {
            expectedCommand = ["/usr/bin/security", "find-generic-password", "-s", SERVICE_NAME, "-D", "Token", "-g"]
            expectedExitCode = 0
        }

        def processFactory = new FifoProcessFactory(
            deleteCredentialSuccess,
            deleteCredentialFailure,
            findNoCredential,
            addCredential,
            findCredential,
            updateCredential,
            findUpdatedCredential,
            deleteTokenSuccess,
            deleteTokenFailure,
            findNoToken,
            addToken,
            findToken,
            updateToken,
            findUpdatedToken,
        )
        endToEndTest(processFactory)
    }

    @Ignore("Needs to be run manually, in interactive mode, because the Keychain needs a desktop")
    @Test public void interactiveInteraction() {
        def processFactory = new DefaultProcessFactory()
        endToEndTest(processFactory)
    }

    static void endToEndTest(final TestableProcessFactory processFactory) {
        final def store = new KeychainSecurityCliStore(processFactory)
        final def credential = new Credential(USER_NAME, PASSWORD)

        // potentially delete an old entry from a previous run of this test
        store.delete(TARGET_NAME)
        // there should be nothing there, yet this call should not fail
        store.delete(TARGET_NAME)

        final def nullCredential = store.readCredentials(TARGET_NAME)

        assert nullCredential == null

        store.writeCredential(TARGET_NAME, credential)

        final def actualCredential = store.readCredentials(TARGET_NAME)

        assert credential == actualCredential

        final def updatedCredential = new Credential(USER_NAME, PASSWORD2)

        store.writeCredential(TARGET_NAME, updatedCredential)

        final def actualUpdatedCredential = store.readCredentials(TARGET_NAME)

        assert updatedCredential == actualUpdatedCredential


        final def token = new Token(PASSWORD, TokenType.Personal)

        // potentially delete an old entry from a previous run of this test
        store.delete(TARGET_NAME)
        // there should be nothing there, yet this call should not fail
        store.delete(TARGET_NAME)

        final def nullToken = store.readToken(TARGET_NAME)

        assert nullToken == null

        store.writeToken(TARGET_NAME, token)

        final def actualToken = store.readToken(TARGET_NAME)

        assert token == actualToken

        final def updatedToken = new Token(PASSWORD2, TokenType.Personal)

        store.writeToken(TARGET_NAME, updatedToken)

        final def actualUpdatedToken = store.readToken(TARGET_NAME)

        assert updatedToken == actualUpdatedToken
    }
}
