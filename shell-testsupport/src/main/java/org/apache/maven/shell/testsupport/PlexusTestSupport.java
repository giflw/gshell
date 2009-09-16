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

package org.apache.maven.shell.testsupport;

import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.DefaultContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;

//
// NOTE: Augmented version of PlexusTestCase to remove dependency on JUnit 3.x style test executions.
//

/**
 * Support for using Plexus components in tests.
 *
 * @version $Rev$ $Date$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class PlexusTestSupport
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private PlexusContainer container;

    private static String basedir;

    private Class owner;

    public PlexusTestSupport(final Class owner) {
        assert owner != null;

        this.owner = owner;
    }

    public PlexusTestSupport(final Object owner) {
        assert owner != null;
        
        this.owner = owner.getClass();
    }

    public Class getOwner() {
        return owner;
    }

    protected void setupContainer() {
        DefaultContext context = new DefaultContext();
        context.put("basedir", getBasedir());
        customizeContext(context);

        boolean hasPlexusHome = context.contains("plexus.home");
        if (!hasPlexusHome) {
            File dir = getTestFile("target/plexus-home");
            if (!dir.isDirectory()) {
                if (!dir.mkdir()) {
                    log.warn("Failed to create directory: {}", dir);
                }
            }

            context.put("plexus.home", dir.getAbsolutePath());
        }

        String config = getCustomConfigurationName();
        @SuppressWarnings({ "unchecked" })
        ContainerConfiguration containerConfiguration = new DefaultContainerConfiguration()
                .setName("test")
                .setContext(context.getContextData());

        if (config != null) {
            containerConfiguration.setContainerConfiguration(config);
        }
        else {
            String resource = getConfigurationName(null);
            containerConfiguration.setContainerConfiguration(resource);
        }

        customizeContainerConfiguration(containerConfiguration);

        try {
            container = new DefaultPlexusContainer(containerConfiguration);
        }
        catch (PlexusContainerException e) {
            throw new RuntimeException("Failed to create plexus container", e);
        }
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    protected void customizeContainerConfiguration(ContainerConfiguration config) {
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    protected void customizeContext(Context context) {
    }

    protected PlexusConfiguration customizeComponentConfiguration() {
        return null;
    }

    protected InputStream getConfiguration() throws Exception {
        return getConfiguration(null);
    }

    protected InputStream getConfiguration(String subname) throws Exception {
        return getResourceAsStream(getConfigurationName(subname));
    }

    protected String getCustomConfigurationName() {
        return null;
    }

    protected String getConfigurationName(String subname) {
        return getOwner().getClass().getName().replace('.', '/') + ".xml";
    }

    protected InputStream getResourceAsStream(String resource) {
        return getOwner().getClass().getResourceAsStream(resource);
    }

    protected ClassLoader getClassLoader() {
        return getOwner().getClass().getClassLoader();
    }

    //
    // Container access
    //

    public PlexusContainer getContainer() {
        if (container == null) {
            setupContainer();
        }

        return container;
    }

    public Object lookup(String key) throws Exception {
        return getContainer().lookup(key);
    }

    public Object lookup(String role, String roleHint) throws Exception {
        return getContainer().lookup(role, roleHint);
    }

    public <T> T lookup(Class<T> type) throws Exception {
        return getContainer().lookup(type);
    }

    public <T> T lookup(Class<T> type, String roleHint) throws Exception {
        return getContainer().lookup(type, roleHint);
    }

    public void release(Object component) throws Exception {
        getContainer().release(component);
    }

    public void destroy() {
        if (container != null) {
            container.dispose();
            container = null;
        }
    }

    //
    // Helpers
    //

    public static String getBasedir() {
        if (basedir != null) {
            return basedir;
        }

        basedir = System.getProperty("basedir");

        if (basedir == null) {
            basedir = new File("").getAbsolutePath();
        }

        return basedir;
    }

    public static File getTestFile(String path) {
        return new File(getBasedir(), path);
    }

    public static File getTestFile(String basedir, String path) {
        File basedirFile = new File(basedir);

        if (!basedirFile.isAbsolute()) {
            basedirFile = getTestFile(basedir);
        }

        return new File(basedirFile, path);
    }

    public static String getTestPath(String path) {
        return getTestFile(path).getAbsolutePath();
    }

    public static String getTestPath(String basedir, String path) {
        return getTestFile(basedir, path).getAbsolutePath();
    }

    public String getTestConfiguration() {
        return getTestConfiguration(getOwner().getClass());
    }

    public static String getTestConfiguration(Class<?> clazz) {
        String s = clazz.getName().replace('.', '/');
        return s.substring(0, s.indexOf("$")) + ".xml";
    }
}