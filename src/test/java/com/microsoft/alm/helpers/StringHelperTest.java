// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.helpers;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class StringHelperTest
{
    public static void assertLinesEqual(final String expected, final String actual) throws IOException
    {
        final StringReader expectedSr = new StringReader(expected);
        final BufferedReader expectedBr = new BufferedReader(expectedSr);
        final StringReader actualSr = new StringReader(actual);
        final BufferedReader actualBr = new BufferedReader(actualSr);

        String expectedLine;
        String actualLine;
        while ((expectedLine = expectedBr.readLine()) != null)
        {
            if ((actualLine = actualBr.readLine()) != null)
            {
                Assert.assertEquals(expectedLine, actualLine);
            }
            else
            {
                Assert.fail("'expected' contained more lines than 'actual'.");
            }
        }
        if ((actualLine = actualBr.readLine()) != null)
        {
            Assert.fail("'actual' contained more lines than 'expected'.");
        }
    }
}
