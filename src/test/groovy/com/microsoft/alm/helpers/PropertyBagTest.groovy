// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers

import groovy.transform.CompileStatic
import org.junit.Test

/**
 * A class to test {@see PropertyBag}.
 */
@CompileStatic
public class PropertyBagTest {

    @Test public void readOptionalInteger_number() {
        final cut = new PropertyBag();
        cut.put("answer", 42.0d);

        final actual = cut.readOptionalInteger("answer", 0);

        assert 42 == actual
    }

    @Test public void readOptionalInteger_string() {
        final cut = new PropertyBag();
        cut.put("answer", "42");

        final actual = cut.readOptionalInteger("answer", 0);

        assert 42 == actual
    }

    @Test public void readOptionalInteger_otherObject() {
        final cut = new PropertyBag();
        cut.put("answer", null);

        final actual = cut.readOptionalInteger("answer", 0);

        assert 0 == actual
    }

    @Test public void readOptionalInteger_missing() {
        final cut = new PropertyBag();

        final actual = cut.readOptionalInteger("answer", 0);

        assert 0 == actual
    }

}
