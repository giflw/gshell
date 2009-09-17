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

/**
 * Common shell variable names.
 *
 * @version $Rev$ $Date$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public interface VariableNames
{
    String MVNSH_HOME = "mvnsh.home";

    String MVNSH_PROGRAM = "mvnsh.program";

    String MVNSH_VERSION = "mvnsh.version";

    String MVNSH_USER_DIR = "mvnsh.user.dir";

    String MVNSH_USER_HOME = "mvnsh.user.home";

    String MVNSH_PROMPT = "mvnsh.prompt";

    String MVNSH_HISTORY = "mvnsh.history";

    String MVNSH_SHOW_STACKTRACE = "mvnsh.show.stacktrace";

    String LAST_RESULT = "_";
}