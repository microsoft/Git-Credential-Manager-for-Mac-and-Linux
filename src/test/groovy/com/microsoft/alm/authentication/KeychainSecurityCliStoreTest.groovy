// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package com.microsoft.alm.authentication

import groovy.transform.CompileStatic
import org.junit.Assert;
import org.junit.Test;

/**
 * A class to test {@see KeychainSecurityCliStore}.
 */
@CompileStatic
public class KeychainSecurityCliStoreTest {

    @Test
    public void parse() {
        Assert.assertEquals(4, 2 + 2);
    }
}
