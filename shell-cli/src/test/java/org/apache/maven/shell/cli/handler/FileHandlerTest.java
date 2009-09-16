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

package org.apache.maven.shell.cli.handler;

import org.apache.maven.shell.cli.CommandLineProcessor;
import org.apache.maven.shell.cli.Option;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Tests for the {@link FileHandler} class.
 *
 * @version $Rev$ $Date$
 */
public class FileHandlerTest
{
    TestBean bean;

    CommandLineProcessor clp;

    @Before
    public void setup() {
        bean = new TestBean();
        clp = new CommandLineProcessor(bean);

        assertEquals(1, clp.getOptionHandlers().size());
        assertEquals(0, clp.getArgumentHandlers().size());
    }

    @After
    public void teardown() {
        bean = null;
        clp = null;
    }

    @Test
    public void test1() throws Exception {
        clp.process("-1", "/tmp");

        File f = new File("/tmp");

        assertEquals(f, bean.f);
    }

    private static class TestBean
    {
        @Option(name="-1")
        File f;
    }
}