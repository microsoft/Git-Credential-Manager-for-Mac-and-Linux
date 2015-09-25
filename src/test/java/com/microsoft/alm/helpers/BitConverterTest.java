package com.microsoft.alm.helpers;

import org.junit.Assert;
import org.junit.Test;

public class BitConverterTest
{
    @Test public void toString_example() throws Exception
    {
        final byte[] bytes = {0x7f, 0x2c, 0x4a, 0x00};

        final String actual = BitConverter.toString(bytes);

        Assert.assertEquals("7F-2C-4A-00", actual);
    }
}
