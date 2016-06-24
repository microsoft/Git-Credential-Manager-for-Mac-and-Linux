// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers

import groovy.transform.CompileStatic
import org.junit.Assert
import org.junit.Test

/**
 * A class to test {@see SimpleJson}.
 */
@CompileStatic
public class SimpleJsonTest {

    private static void assertParse(final Map<String, ? extends Object> expected, final String input) {
        final actual = SimpleJson.parse(input)

        assert expected == actual
    }

    private static void assertParseError(final String expectedMessage, final String input) {
        try {
            SimpleJson.parse(input)
        }
        catch (final IllegalArgumentException actual) {
            def actualMessage = actual.message
            assert expectedMessage == actualMessage
            return;
        }
        Assert.fail("Expected IllegalArgumentException with message: " + expectedMessage)
    }

    @Test public void parse_emptyString() {
        assertParse([:], "")
    }

    @Test public void parse_emptyMap() {
        assertParse([:], "{}")
    }

    @Test public void parse_singleString() {
        assertParse(["name":"value"], /{"name":"value"}/)
        assertParse(["name":"value"], /{"name":"value",}/)
    }

    @Test public void parse_singleSquareBracketString() {
        assertParse(["name":'"value"'], /{"name":["value"]}/)
        assertParse(["name":'"value"'], /{"name":["value"],}/)
        assertParse(["error_codes":'50001'], /{"error_codes":[50001]}/)
    }

    @Test public void parse_insignificantWhitespace() {
        assertParse(["name":"value"], /{"name":"value" ,}/)
        assertParse(["name":"value"], /{"name":"value", }/)
        assertParse(["name":"value"], /{"name" :"value"}/)
        assertParse(["name":"value"], '{\t"name"\r:\t"value"\n}')
    }

    @Test public void parse_escapedString() {
        assertParse(["name":"/\b\f\"\n\r\t\u20AC\\"], '{"name":"\\/\\b\\f\\"\\n\\r\\t\\u20AC\\\\"}')
    }

    @Test public void parse_singleNumber() {
        assertParse(["answer":42], /{"answer":42}/)
        assertParse(["answer":42], /{"answer":42,}/)
        assertParse(["answer":42], /{"answer":42 ,}/)
    }

    @Test public void parse_singleNegativeNumber() {
        assertParse(["answer":-42], /{"answer":-42}/)
    }

    @Test public void parse_singleFractionalNumber() {
        assertParse(["answer":4.2], /{"answer":4.2}/)
    }

    @Test public void parse_singleExponentialNumber() {
        assertParse(["answer":4e1], /{"answer":4e1}/)
        assertParse(["answer":4e1], /{"answer":4E1}/)
        assertParse(["answer":4e1], /{"answer":4E+1}/)
        assertParse(["answer":4e-1], /{"answer":4E-1}/)
    }

    @Test public void parse_singleLiteral() {
        assertParse(["answer":true], /{"answer":true}/)
        assertParse(["answer":true], /{"answer":true,}/)
        assertParse(["answer":false], /{"answer":false}/)
        assertParse(["answer":false], /{"answer":false,}/)
        assertParse(["answer":null], /{"answer":null}/)
        assertParse(["answer":null], /{"answer":null,}/)
    }

    @Test public void parse_deviceEndpointExampleResponse() {
        final input = """
  {
    "device_code":"74tq5miHKB",
    "user_code":"94248",
    "verification_uri":"https://www.example.com/device",
    "interval":5
  }
"""
        final def expected = [
            "device_code":"74tq5miHKB",
            "user_code":"94248",
            "verification_uri":"https://www.example.com/device",
            "interval":5
        ]

        assertParse(expected, input)
    }

    @Test public void parse_commaAfterNumberIsPreKey() {
        final input = """
    {
        "access_token":"2YotnFZFEjr1zCsicMWpAA",
        "token_type":"example",
        "expires_in":3600,
        "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
        "example_parameter":"example_value"
    }
"""
        final def expected = [
            "access_token":"2YotnFZFEjr1zCsicMWpAA",
            "token_type":"example",
            "expires_in":3600,
            "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
            "example_parameter":"example_value"
        ]

        assertParse(expected, input)
    }

    @Test public void parse_error_START() {
        assertParseError("Unexpected character '[' at state START.", /[/)
    }

    @Test public void parse_error_PRE_KEY() {
        assertParseError("Unexpected character '[' at state PRE_KEY.", /{[/)
    }

    @Test public void parse_error_PRE_VALUE() {
        assertParseError("Unexpected character '=' at state PRE_VALUE.", /{"key"=/)
    }

    @Test public void parse_error_VALUE() {
        assertParseError("Unexpected character '=' at state VALUE.", /{"key":=/)
    }

    @Test public void parse_error_NUMBER_VALUE() {
        assertParseError("Unexpected character 'a' at state NUMBER_VALUE.", /{"key":3a/)
    }

    @Test public void parse_error_STRING_VALUE_ESCAPE() {
        assertParseError("Unexpected character '?' at state STRING_VALUE_ESCAPE.", /{"key":"\?/)
    }

    @Test public void parse_error_STRING_VALUE_UNICODE() {
        assertParseError("Unexpected character 'x' at state STRING_VALUE_UNICODE.", '{"key":"\\ux')
    }

    @Test public void parse_error_LITERAL_VALUE() {
        assertParseError("Unexpected character 't' at state LITERAL_VALUE.", /{"key":tt/)
    }

    @Test public void parse_error_POST_VALUE() {
        assertParseError("Unexpected character ';' at state POST_VALUE.", /{"key":"value";/)
    }

    @Test public void parse_error_END() {
        assertParseError("Unexpected character ';' at state END.", /{"key":"value"};/)
    }
}
