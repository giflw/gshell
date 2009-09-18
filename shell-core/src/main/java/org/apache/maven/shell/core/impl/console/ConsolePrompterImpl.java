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

package org.apache.maven.shell.core.impl.console;

import org.apache.maven.shell.VariableNames;
import org.apache.maven.shell.Variables;
import org.apache.maven.shell.Branding;
import org.apache.maven.shell.ansi.AnsiRenderer;
import org.apache.maven.shell.console.Console;
import org.codehaus.plexus.interpolation.AbstractValueSource;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.Interpolator;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.codehaus.plexus.interpolation.PrefixedObjectValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Console.Prompter} component.
 *
 * @version $Rev$ $Date$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ConsolePrompterImpl
    implements Console.Prompter, VariableNames
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String SHELL_BRANDING = "shell.branding";

    private static final String DEFAULT_PROMPT = "> ";

    // NOTE: Have to use %{} here to avoid causing problems with ${} variable interpolation

    private final Interpolator interp = new StringSearchInterpolator("%{", "}");

    private final AnsiRenderer renderer = new AnsiRenderer();

    private final Variables vars;

    private final Branding branding;

    public ConsolePrompterImpl(final Variables vars, final Branding branding) {
        assert vars != null;
        assert branding != null;
        this.vars = vars;
        this.branding = branding;

        interp.addValueSource(new AbstractValueSource(false) {
            public Object getValue(final String expression) {
                return vars.get(expression);
            }
        });
        interp.addValueSource(new PrefixedObjectValueSource(SHELL_BRANDING, branding));
        interp.addValueSource(new PropertiesBasedValueSource(System.getProperties()));
    }

    public String prompt() {
        String prompt = null;

        String pattern = vars.get(SHELL_PROMPT, String.class);
        if (pattern != null) {
            try {
                prompt = interp.interpolate(pattern);
            }
            catch (InterpolationException e) {
                log.warn("Failed to render prompt pattern: " + pattern, e);
            }
        }

        // Use a default prompt if we don't have anything here
        if (prompt == null) {
            // TODO: Maybe use branding.getDefaultPrompt() here, but have to interpolate
            //       and then handle null again, defaulting to this basic prompt?
            prompt = DEFAULT_PROMPT;
        }

        // Encode ANSI muck if it looks like there are codes encoded
        if (AnsiRenderer.test(prompt)) {
            prompt = renderer.render(prompt);
        }

        return prompt;
    }
}