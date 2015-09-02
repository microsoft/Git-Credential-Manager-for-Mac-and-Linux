package com.microsoft.alm.java.git_credential_helper.helpers;

import org.junit.Assert;
import org.junit.Test;

public class StringHelperTest
{
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
}
