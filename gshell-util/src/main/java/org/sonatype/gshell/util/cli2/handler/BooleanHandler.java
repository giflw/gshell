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
package org.sonatype.gshell.util.cli2.handler;

import org.sonatype.gshell.util.cli2.CliDescriptor;
import org.sonatype.gshell.util.cli2.OptionDescriptor;

/**
 * Handler for boolean types.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.3
 */
public class BooleanHandler
    extends Handler
{
    public BooleanHandler(final CliDescriptor desc) {
        super(desc);
    }

    @Override
    public void handle(final String arg) throws Exception {
        if (getDescriptor().isArgument()) {
            set(arg);
        }
        else {
            OptionDescriptor opt = (OptionDescriptor) getDescriptor();
            if (opt.isArgumentOptional() && arg != null) {
                set(arg);
            }
            else {
                set(true);
            }
        }
    }

    @Override
    public String getDefaultToken() {
        if (getDescriptor().isArgument() || ((OptionDescriptor)getDescriptor()).getArgs() > 0) {
            return "FLAG"; // TODO: i18n
        }
        
        return null;
    }
}