package com.microsoft.alm.authentication;

import org.junit.Assert;
import org.junit.Test;

public class GlobalTest
{
    @Test
    public void getUserAgent_correctFormat() throws Exception
    {
        final String actual = Global.getUserAgent();

        Assert.assertNotNull(actual);
        Assert.assertTrue(actual.matches("git-credential-manager \\([^;]+; [^;]+; [^;]+\\) [^/]+/.+ git-tools/.+"));
    }
}
