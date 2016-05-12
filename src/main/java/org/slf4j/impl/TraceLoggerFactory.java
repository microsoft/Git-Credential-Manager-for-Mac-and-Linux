// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An implementation of {@link ILoggerFactory} which always returns
 * {@link TraceLogger} instances.
 */
public class TraceLoggerFactory implements ILoggerFactory
{
    private final ConcurrentMap<String, Logger> loggersByName = new ConcurrentHashMap<String, Logger>();

    @Override
    public Logger getLogger(final String name)
    {
        Logger simpleLogger = loggersByName.get(name);
        if (simpleLogger != null) {
            return simpleLogger;
        } else {
            Logger newInstance = new TraceLogger(name);
            Logger oldInstance = loggersByName.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }
}
