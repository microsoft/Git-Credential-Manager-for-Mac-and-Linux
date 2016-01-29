/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See License.txt in the project root.
 */

package com.microsoft.alm.authentication

import com.microsoft.alm.gitcredentialmanager.TestProcess

class FifoProcess extends TestProcess {

    String[] expectedCommand = null;
    int expectedExitCode = 0;
    String expectedStandardInput = null;

    public FifoProcess(final String input) {
        super(input)
    }

    public FifoProcess(final String input, final String error) {
        super(input, error)
    }

    @Override
    int waitFor() throws InterruptedException {
        if (expectedStandardInput != null) {
            final def actualStandardInput = getOutput()
            assert actualStandardInput == expectedStandardInput
        }
        return expectedExitCode
    }
}
