/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See License.txt in the project root.
 */

package com.microsoft.alm.authentication

import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcess
import com.microsoft.alm.oauth2.useragent.subprocess.TestableProcessFactory

class FifoProcessFactory implements TestableProcessFactory {

    private final List<FifoProcess> processes = new ArrayList<>();

    public FifoProcessFactory(final FifoProcess... processes) {
        this.processes.addAll(processes)
    }

    @Override TestableProcess create(final String... command) throws IOException {
        def process = this.processes.remove(0)

        if (process.expectedCommand != null) {
            def expectedCommandList = process.expectedCommand.toList()
            def actualCommandList = command.toList()
            assert expectedCommandList == actualCommandList
        }

        return process
    }
}
