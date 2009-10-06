/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.gshell.ansi;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Locale;

/**
 * Print writer implementation which supports automatic ANSI color rendering.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 *
 * @since 2.0
 */
public class AnsiRendererWriter
    extends PrintWriter
{
    private final AnsiRenderer renderer = new AnsiRenderer();

    public AnsiRendererWriter(final OutputStream out) {
        super(out);
    }

    public AnsiRendererWriter(final OutputStream out, final boolean autoFlush) {
        super(out, autoFlush);
    }

    public AnsiRendererWriter(final Writer out) {
        super(out);
    }

    public AnsiRendererWriter(final Writer out, final boolean autoFlush) {
        super(out, autoFlush);
    }

    @Override
    public void write(final String s) {
        if (AnsiRenderer.test(s)) {
            super.write(renderer.render(s));
        }
        else {
            super.write(s);
        }
    }

    //
    // Need to prevent partial output from being written while formatting or we will get rendering exceptions
    //
    
    @Override
    public PrintWriter format(final String format, final Object... args) {
        print(String.format(format, args));
        return this;
    }

    @Override
    public PrintWriter format(final Locale l, final String format, final Object... args) {
        print(String.format(l, format, args));
        return this;
    }
}
