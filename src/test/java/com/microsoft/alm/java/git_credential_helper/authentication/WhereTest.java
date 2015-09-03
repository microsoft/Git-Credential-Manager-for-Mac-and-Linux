package com.microsoft.alm.java.git_credential_helper.authentication;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public class WhereTest
{
    @Test public void app_ping() throws Exception
    {
        final AtomicReference<String> path = new AtomicReference<String>();

        final boolean success = Where.app("ping", path);
        final String actual = path.get();

        Assert.assertEquals("Doesn't ping exist on all platforms?", true, success);
        Assert.assertNotNull(actual);
    }

}
