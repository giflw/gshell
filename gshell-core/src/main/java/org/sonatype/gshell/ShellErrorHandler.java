/*
 * Copyright (C) 2009 the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sonatype.gshell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.console.ConsoleErrorHandler;
import org.sonatype.gshell.notification.ErrorNotification;
import org.sonatype.gshell.util.i18n.MessageSource;
import org.sonatype.gshell.util.i18n.ResourceBundleMessageSource;

/**
 * Shell {@link ConsoleErrorHandler} which renders errors with ANSI codes.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
public class ShellErrorHandler
    implements ConsoleErrorHandler, VariableNames
{
    private static final Logger log = LoggerFactory.getLogger(ShellErrorHandler.class);

    private static enum Messages
    {
        ERROR_EXCEPTION_NAME,
        ERROR_EXCEPTION_AT,
        ERROR_EXCEPTION_CAUSED_BY,
        ERROR_LOCATION_NATIVE,
        ERROR_LOCATION_UNKNOWN;
        
        private static final MessageSource messages = new ResourceBundleMessageSource(ShellErrorHandler.class);

        String format(final Object... args) {
            return messages.format(name(), args);
        }
    }
    
    private final IO io;

    public ShellErrorHandler(final IO io) {
        assert io != null;
        this.io = io;
    }

    public boolean handleError(final Throwable error) {
        assert error != null;
        displayError(error);
        return true;
    }

    private void displayError(final Throwable error) {
        assert error != null;

        // Decode any error notifications
        Throwable cause = error;
        if (error instanceof ErrorNotification) {
            cause = error.getCause();
        }

        Variables vars = ShellHolder.get().getVariables();

        // Determine if the stack trace flag is set
        boolean showTrace = false;
        if (vars.contains(SHELL_ERRORS)) {
            String tmp = vars.get(SHELL_ERRORS, String.class);
            showTrace = Boolean.parseBoolean(tmp.trim());
        }

        // Spit out the terse reason why we've failed
        io.err.println(Messages.ERROR_EXCEPTION_NAME.format(cause.getClass().getName(), cause.getMessage()));

        if (showTrace || io.isVerbose()) {
            while (cause != null) {
                for (StackTraceElement e : cause.getStackTrace()) {
                    io.err.print("    ");
                    io.err.println(Messages.ERROR_EXCEPTION_AT.format(e.getClassName(), e.getMethodName(), getLocation(e)));
                }

                cause = cause.getCause();
                if (cause != null) {
                    io.err.println(Messages.ERROR_EXCEPTION_CAUSED_BY.format(cause.getClass().getName(), cause.getMessage()));
                }
            }
        }

        io.err.flush();
    }

    private String getLocation(final StackTraceElement e) {
        assert e != null;

        if (e.isNativeMethod()) {
            return Messages.ERROR_LOCATION_NATIVE.format();
        }
        else if (e.getFileName() == null) {
            return Messages.ERROR_LOCATION_UNKNOWN.format();
        }
        else if (e.getLineNumber() >= 0) {
            return String.format("%s:%s", e.getFileName(), e.getLineNumber());
        }
        else {
            return e.getFileName();
        }
    }
}