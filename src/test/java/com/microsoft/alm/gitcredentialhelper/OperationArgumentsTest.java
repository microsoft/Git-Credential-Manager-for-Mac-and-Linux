// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.gitcredentialhelper;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

public class OperationArgumentsTest
{
    @Test
    public void typical() throws IOException, URISyntaxException
    {
        final String input = "protocol=https\n" +
                "host=example.visualstudio.com\n" +
                "path=path\n" +
                "username=userName\n" +
                "password=incorrect\n" +
                "";

        final BufferedReader br = new BufferedReader(new StringReader(input));
        OperationArguments cut;
        try
        {
            cut = new OperationArguments(br);
        }
        finally
        {
            br.close();
        }

        Assert.assertEquals("https", cut.Protocol);
        Assert.assertEquals("example.visualstudio.com", cut.Host);
        Assert.assertEquals("https://example.visualstudio.com/", cut.TargetUri.toString());
        Assert.assertEquals("path", cut.Path);
        Assert.assertEquals("userName", cut.getUserName());
        Assert.assertEquals("incorrect", cut.getPassword());

        final List<String> expected = readLines(input);
        final List<String> actual = readLines(cut.toString());
        Assert.assertThat(actual, is(expected));
    }

    private static List<String> readLines(final String input) throws IOException
    {
        final ArrayList<String> result = new ArrayList<String>();
        final BufferedReader br = new BufferedReader(new StringReader(input));
        try
        {
            String line;
            while ((line = br.readLine()) != null)
            {
                result.add(line);
            }
        }
        finally
        {
            br.close();
        }
        return result;
    }
}
