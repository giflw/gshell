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
package org.sonatype.gshell.commands.pref;

import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.util.io.Closer;
import org.sonatype.gshell.util.io.Flusher;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.util.cli2.Option;
import org.sonatype.gshell.util.pref.Preference;
import org.sonatype.gshell.util.pref.Preferences;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Export preference nodes to a file.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
@Command(name="pref/export")
@Preferences(path="commands/pref/export")
public class ExportPreferencesCommand
    extends PreferenceNodeCommandSupport
{
    @Preference
    @Option(name = "t", longName="subtree")
    private boolean subTree;

    @Argument(index = 1)
    private File file;

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();
        java.util.prefs.Preferences prefs = node();

        OutputStream out;
        if (file == null) {
            out = io.streams.out;
        }
        else {
            io.println("Exporting preferences to: {}", file); // TODO: i18n
            out = new BufferedOutputStream(new FileOutputStream(file));
        }

        try {
            if (subTree) {
                prefs.exportSubtree(out);
            }
            else {
                prefs.exportNode(out);
            }

            Flusher.flush(out);
        }
        finally {
            if (file != null) {
                Closer.close(out);
            }
        }

        prefs.sync();
        
        return Result.SUCCESS;
    }
}