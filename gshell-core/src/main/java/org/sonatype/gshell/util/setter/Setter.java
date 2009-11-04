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

package org.sonatype.gshell.util.setter;

import org.sonatype.gshell.cli.ProcessingException;

/**
 * Provides the basic mechanism to set values.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface Setter
{
    /**
     * Adds/sets a value to the property of the option bean.
     *
     * <p>
     * A {@link Setter} object has an implicit knowledge about the property it's setting,
     * and the instance of the option bean.
     */
    void set(Object value) throws ProcessingException;

    String getName();
    
    /**
     * Gets the type of the underlying method/field.
     */
    Class getType();
    
    /**
     * Whether this setter is instrinsically multi-valued.
     */
    boolean isMultiValued();
}
