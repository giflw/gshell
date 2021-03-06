/*
 * Copyright (c) 2009-2013 the original author or authors.
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
package org.sonatype.gshell.commands.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.support.CommandActionSupport;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.util.io.PumpStreamHandler;
import org.sonatype.gshell.util.cli2.Argument;

import java.util.List;

/**
 * Execute system processes.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
@Command(name="exec")
public class ExecuteCommand
    extends CommandActionSupport
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Argument(required=true)
    private List<String> args;

    // TODO: Support setting the process directory and environment muck

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();

        ProcessBuilder builder = new ProcessBuilder(args);

        log.info("Executing: {}", builder.command());

        Process p = builder.start();

        PumpStreamHandler handler = new PumpStreamHandler(io.streams);
        handler.attach(p);
        handler.start();

        log.debug("Waiting for process to exit...");

        int status = p.waitFor();

        log.info("Process exited w/status: {}", status);

        handler.stop();

        return status;
    }
}