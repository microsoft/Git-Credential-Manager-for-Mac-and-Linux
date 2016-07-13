// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the project root.

// A lot of the code from this file was inspired from slf4j-simple's SimpleLogger class
// and do its copyright and permission notices (MIT License) are included below.
/**
 * Copyright (c) 2004-2012 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package org.slf4j.impl;

import com.microsoft.alm.helpers.Trace;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LocationAwareLogger;

public class TraceLogger extends MarkerIgnoringBase
{
    /** The current log level */
    protected int currentLogLevel = LocationAwareLogger.DEBUG_INT;

    TraceLogger(final String name)
    {
        this.name = name;
    }

    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel is this level enabled?
     *
     * @return {@code true} if the specified logLevel is enabled;
     *         {@code false} otherwise.
     */
    protected boolean isLevelEnabled(final int logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return (logLevel >= currentLogLevel);
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     */
    private void formatAndLog(final int level, final String format, final Object arg1, final Object arg2) {
        if (!isLevelEnabled(level)) {
            return;
        }
        FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     */
    private void formatAndLog(final int level, final String format, final Object... arguments) {
        if (!isLevelEnabled(level)) {
            return;
        }
        FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    private void log(final int level, final String message, final Throwable throwable)
    {
        if (!isLevelEnabled(level)) {
            return;
        }
        if (throwable != null)
        {
            Trace.writeLine(message, throwable);
        }
        else
        {
            Trace.writeLine(message);
        }
    }

    @Override
    public boolean isTraceEnabled()
    {
        return isLevelEnabled(LocationAwareLogger.TRACE_INT);
    }

    @Override
    public void trace(final String msg)
    {
        log(LocationAwareLogger.TRACE_INT, msg, null);
    }

    @Override
    public void trace(final String format, final Object arg)
    {
        formatAndLog(LocationAwareLogger.TRACE_INT, format, arg, null);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2)
    {
        formatAndLog(LocationAwareLogger.TRACE_INT, format, arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... arguments)
    {
        formatAndLog(LocationAwareLogger.TRACE_INT, format, arguments);
    }

    @Override
    public void trace(final String msg, final Throwable t)
    {
        log(LocationAwareLogger.TRACE_INT, msg, t);
    }

    @Override
    public boolean isDebugEnabled()
    {
        return isLevelEnabled(LocationAwareLogger.DEBUG_INT);
    }

    @Override
    public void debug(final String msg)
    {
        log(LocationAwareLogger.DEBUG_INT, msg, null);
    }

    @Override
    public void debug(final String format, final Object arg)
    {
        formatAndLog(LocationAwareLogger.DEBUG_INT, format, arg, null);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2)
    {
        formatAndLog(LocationAwareLogger.DEBUG_INT, format, arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... arguments)
    {
        formatAndLog(LocationAwareLogger.DEBUG_INT, format, arguments);
    }

    @Override
    public void debug(final String msg, final Throwable t)
    {
        log(LocationAwareLogger.DEBUG_INT, msg, t);
    }

    @Override
    public boolean isInfoEnabled()
    {
        return isLevelEnabled(LocationAwareLogger.INFO_INT);
    }

    @Override
    public void info(final String msg)
    {
        log(LocationAwareLogger.INFO_INT, msg, null);
    }

    @Override
    public void info(final String format, final Object arg)
    {
        formatAndLog(LocationAwareLogger.INFO_INT, format, arg, null);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2)
    {
        formatAndLog(LocationAwareLogger.INFO_INT, format, arg1, arg2);
    }

    @Override
    public void info(final String format, final Object... arguments)
    {
        formatAndLog(LocationAwareLogger.INFO_INT, format, arguments);
    }

    @Override
    public void info(final String msg, final Throwable t)
    {
        log(LocationAwareLogger.INFO_INT, msg, t);
    }

    @Override
    public boolean isWarnEnabled()
    {
        return isLevelEnabled(LocationAwareLogger.WARN_INT);
    }

    @Override
    public void warn(final String msg)
    {
        log(LocationAwareLogger.WARN_INT, msg, null);
    }

    @Override
    public void warn(final String format, final Object arg)
    {
        formatAndLog(LocationAwareLogger.WARN_INT, format, arg, null);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2)
    {
        formatAndLog(LocationAwareLogger.WARN_INT, format, arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... arguments)
    {
        formatAndLog(LocationAwareLogger.WARN_INT, format, arguments);
    }

    @Override
    public void warn(final String msg, final Throwable t)
    {
        log(LocationAwareLogger.WARN_INT, msg, t);
    }

    @Override
    public boolean isErrorEnabled()
    {
        return isLevelEnabled(LocationAwareLogger.ERROR_INT);
    }

    @Override
    public void error(final String msg)
    {
        log(LocationAwareLogger.ERROR_INT, msg, null);
    }

    @Override
    public void error(final String format, final Object arg)
    {
        formatAndLog(LocationAwareLogger.ERROR_INT, format, arg, null);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2)
    {
        formatAndLog(LocationAwareLogger.ERROR_INT, format, arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... arguments)
    {
        formatAndLog(LocationAwareLogger.ERROR_INT, format, arguments);
    }

    @Override
    public void error(final String msg, final Throwable t)
    {
        log(LocationAwareLogger.ERROR_INT, msg, t);
    }
}
