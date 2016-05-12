// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package org.slf4j.impl

import com.microsoft.alm.helpers.Trace
import groovy.transform.CompileStatic
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * A class to test {@link TraceLogger}.
 */
@CompileStatic
public class TraceLoggerTest {

    @Before @After public void clearTraceListeners() {
        Trace.listeners.clear();
    }

    @Test public void debugLogging() {
        final baos = new ByteArrayOutputStream();
        final destination = new PrintStream(baos);
        Trace.listeners.add(destination);
        final logger = LoggerFactory.getLogger(TraceLoggerTest.class);

        logger.debug("The {} {} fox jumps over the {} dog's back.", "quick", "brown", "lazy");

        final actual = baos.toString().trim();
        assert "The quick brown fox jumps over the lazy dog's back." == actual;
    }
}
