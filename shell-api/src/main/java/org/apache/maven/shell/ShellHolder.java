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

package org.apache.maven.shell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Shell} thread context holder.
 * 
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class ShellHolder
{
    private static final Logger log = LoggerFactory.getLogger(ShellHolder.class);

    private static final InheritableThreadLocal<Shell> holder = new InheritableThreadLocal<Shell>();

    public static Shell set(final Shell shell) {
        log.trace("Setting shell: {}", shell);

        Shell last = holder.get();

        holder.set(shell);

        return last;
    }

    public static Shell get(final boolean allowNull) {
        Shell shell = holder.get();

        log.trace("Getting shell ({}): {}", allowNull, shell);

        if (!allowNull && shell == null) {
            throw new IllegalStateException("Shell not initialized for thread: " + Thread.currentThread());
        }

        return shell;
    }

    public static Shell get() {
        return get(false);
    }
}