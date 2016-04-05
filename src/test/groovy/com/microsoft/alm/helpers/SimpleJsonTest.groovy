// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers

import groovy.transform.CompileStatic
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

    @Test public void parse_emptyString() {
        assertParse([:], "")
    }

}
