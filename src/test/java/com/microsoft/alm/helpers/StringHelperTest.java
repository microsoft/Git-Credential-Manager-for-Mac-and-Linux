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
    @Test public void endsWithIgnoreCase_positive()
    {
        Assert.assertTrue(StringHelper.endsWithIgnoreCase("Session", "Session"));
        Assert.assertTrue(StringHelper.endsWithIgnoreCase("Session", ""));
        Assert.assertTrue(StringHelper.endsWithIgnoreCase("Session", "SiOn"));
    }

    @Test public void endsWithIgnoreCase_negative()
    {
        Assert.assertFalse(StringHelper.endsWithIgnoreCase("Session", "SiO"));
        Assert.assertFalse(StringHelper.endsWithIgnoreCase("Session", "LongerThanSession"));
        Assert.assertFalse(StringHelper.endsWithIgnoreCase("Session", "noisseS"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void endsWithIgnoreCase_firstNull()
    {
        Assert.assertTrue(StringHelper.endsWithIgnoreCase(null, "SiO"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void endsWithIgnoreCase_secondNull()
    {
        Assert.assertTrue(StringHelper.endsWithIgnoreCase("Session", null));
    }

    @Test public void isNullOrWhiteSpace_null()
    {
        Assert.assertTrue(StringHelper.isNullOrWhiteSpace(null));
    }

    @Test public void isNullOrWhiteSpace_empty()
    {
        Assert.assertTrue(StringHelper.isNullOrWhiteSpace(StringHelper.Empty));
    }

    @Test public void isNullOrWhiteSpace_whiteSpace()
    {
        Assert.assertTrue(StringHelper.isNullOrWhiteSpace(" "));
        Assert.assertTrue(StringHelper.isNullOrWhiteSpace("\n"));
        Assert.assertTrue(StringHelper.isNullOrWhiteSpace("\t"));
    }

    @Test public void isNullOrWhiteSpace_content()
    {
        Assert.assertFalse(StringHelper.isNullOrWhiteSpace("isNullOrWhiteSpace"));
        Assert.assertFalse(StringHelper.isNullOrWhiteSpace(" isNullOrWhiteSpace"));
        Assert.assertFalse(StringHelper.isNullOrWhiteSpace("isNullOrWhiteSpace "));
        Assert.assertFalse(StringHelper.isNullOrWhiteSpace(" isNullOrWhiteSpace "));
    }

    @Test public void join_typical()
    {
        final String[] a = {"a", "b", "c"};

        final String actual = StringHelper.join(",", a, 0, a.length);

        Assert.assertEquals("a,b,c", actual);
    }

    @Test public void join_edge_oneElementInArray()
    {
        final String[] a = {"a"};

        final String actual = StringHelper.join(",", a, 0, a.length);

        Assert.assertEquals("a", actual);
    }

    @Test public void join_skipFirst()
    {
        final String[] a = {"a", "b", "c"};

        final String actual = StringHelper.join(",", a, 1, a.length - 1);

        Assert.assertEquals("b,c", actual);
    }

    @Test public void join_skipLast()
    {
        final String[] a = {"a", "b", "c"};

        final String actual = StringHelper.join(",", a, 0, a.length - 1);

        Assert.assertEquals("a,b", actual);
    }

    @Test public void join_typical_simpleOverload()
    {
        final String[] a = {"a", "b", "c"};

        final String actual = StringHelper.join(",", a);

        Assert.assertEquals("a,b,c", actual);
    }

    @Test public void join_returnsStringEmptyIfCountZero()
    {
        final String[] a = {"a", "b", "c"};

        Assert.assertEquals(StringHelper.Empty, StringHelper.join(",", a, 0, 0));
    }

    @Test public void join_returnsStringEmptyIfValueHasNoElements()
    {
        final String[] emptyArray = {};

        Assert.assertEquals(StringHelper.Empty, StringHelper.join(",", emptyArray, 0, 0));
    }

    @Test public void join_returnsStringEmptyIfSeparatorAndAllElementsAreEmpty()
    {
        final String[] arrayOfEmpty = {StringHelper.Empty, StringHelper.Empty, StringHelper.Empty};

        Assert.assertEquals(StringHelper.Empty, StringHelper.join(StringHelper.Empty, arrayOfEmpty, 0, 3));
    }

    @Test public void join_withQuotingProcessor()
    {
        final Func<String, String> quotingProcessor = new Func<String, String>()
        {
            @Override public String call(final String s)
            {
                if (s.contains(" "))
                {
                    return '"' + s + '"';
                }
                return s;
            }
        };
        final String[] args = {"--user", "man-with-hat", "--password", "battery horse staple correct"};

        final String actual = StringHelper.join(" ", args, 0, args.length, quotingProcessor);

        Assert.assertEquals("--user man-with-hat --password \"battery horse staple correct\"", actual);
    }

    @Test public void trimEnd_documentationExample()
    {
        final String actual = StringHelper.trimEnd("123abc456xyz789", '1', '2', '3', '4', '5', '6', '7', '8', '9');
        Assert.assertEquals("123abc456xyz", actual);
    }

    @Test public void trimEnd_edgeCases()
    {
        Assert.assertEquals("", StringHelper.trimEnd("", ' ', '\t'));
        Assert.assertEquals("", StringHelper.trimEnd(" ", ' ', '\t'));
        Assert.assertEquals("a", StringHelper.trimEnd("a", ' '));
        Assert.assertEquals("a", StringHelper.trimEnd("a", ' ', '\t'));
        Assert.assertEquals("a", StringHelper.trimEnd("a ", ' '));
        Assert.assertEquals("a", StringHelper.trimEnd("a ", ' ', '\t'));
        Assert.assertEquals("a", StringHelper.trimEnd("a\t", ' ', '\t'));
        Assert.assertEquals("a", StringHelper.trimEnd("a \t", ' ', '\t'));
        Assert.assertEquals(" trimEnd \n", StringHelper.trimEnd(" trimEnd \n\t", ' ', '\t'));
        Assert.assertEquals(" trimEnd", StringHelper.trimEnd(" trimEnd ", ' ', '\t'));
    }

    @Test public void trimEnd_defaultWhitespace()
    {
        Assert.assertEquals("trimEnd", StringHelper.trimEnd("trimEnd"));
        Assert.assertEquals("trimEnd", StringHelper.trimEnd("trimEnd "));
        Assert.assertEquals("trimEnd", StringHelper.trimEnd("trimEnd ", null));
        Assert.assertEquals("trimEnd", StringHelper.trimEnd("trimEnd ", new char[]{}));
        Assert.assertEquals(" trimEnd", StringHelper.trimEnd(" trimEnd"));
        Assert.assertEquals(" trimEnd", StringHelper.trimEnd(" trimEnd "));
        Assert.assertEquals("", StringHelper.trimEnd(""));
        Assert.assertEquals("", StringHelper.trimEnd(" "));
    }

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
