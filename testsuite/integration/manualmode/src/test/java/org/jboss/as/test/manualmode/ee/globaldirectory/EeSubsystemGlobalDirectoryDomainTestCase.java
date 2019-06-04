/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.test.manualmode.ee.globaldirectory;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.helpers.Operations;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOVE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESTART;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SHUTDOWN;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
//import java.net.URL;

/**
 * @author Vratislav Marek (vmarek@redhat.com)
 **/
@RunWith(Arquillian.class)
@RunAsClient
public class EeSubsystemGlobalDirectoryDomainTestCase extends EESubsystemGlobalDirectory {

    private static final String CONTAINER = "default-jbossas";
    /*Global Directory Name*/
    private static final String GDN = "ShareLib";

    protected static final Logger LOGGER = Logger.getLogger(EeSubsystemGlobalDirectoryDomainTestCase.class);

    @ArquillianResource
    protected static ContainerController containerController;

    @ArquillianResource
    protected static Deployer deployer;

    protected static Solver solver;

    @BeforeClass
    public static void beforeClass() {
        solver = new Solver();
    }

    @Before
    public void before() throws IOException {
        if (!containerController.isStarted(CONTAINER)) {
            containerController.start(CONTAINER);
            solver.prepare();
        }
    }

    @After
    public void after() {
        if (containerController.isStarted(CONTAINER)) {
            containerController.stop(CONTAINER);
            solver.clear();
        }
    }

/*
    Major functionality

    org.jboss.as.test.manualmode.ee.globaldirectory.EeSubsystemGlobalDirectoryDomainTestCase#testAppSharedLib

    Test prerequisites
        Create temporary directory and include test jars dependency of test deployment application to all domain servers
    Set logging level to DEBUG
    Define global-directory by CLI command
    Check if global-directory is registered properly
    Restart domain
    Check log file if contains all jars with alphabetical order
    Deploy test application deployment
    Call some method from global-directory in deployment and verify method output
 */

    @Test
    public void testAppSharedLib() throws IOException {
//        solver.copyLibraries(null);
//        solver.register(GDN);
//        solver.verifyProperlyRegistered(GDN, solver.getLibraryPath());
        solver.restartServer();
        solver.restartServer();
        solver.checkLogs(null);

    }

    protected static class Solver {

        protected static final String GLOBAL_DIRECTORY_PREFIX = "GlDir";

        private static final String tempDirPath = TestSuiteEnvironment.getTmpDir();
        private static final File tempDir = new File(tempDirPath);

        private File library;
        private ClientHolder client;

        protected final void prepare() throws IOException {
            createLibDir();
            connect();
        }

        protected final void clear() {
            if (library != null) {
                library.delete();
            }
            client = null;
        }

        protected void connect() {
            if (client == null) {
                client = ClientHolder.init();
            }
        }

        protected void createLibDir() throws IOException {
            createLibDir(GLOBAL_DIRECTORY_PREFIX);
        }

        protected void createLibDir(String prefix) throws IOException {
            if (library == null || !library.exists()) {
                File file = File.createTempFile(prefix, "", tempDir);
                file.delete();
                file.mkdir();
                library = file;
            }
        }

        protected String getLibraryPath() {
            return library.getAbsolutePath();
        }

        protected void restartServer() throws IOException {
            // TODO fix it - doens't reaction to restart, only shutdown
            // shutdown --restart
            final ModelNode operation = Operations.createOperation(SHUTDOWN);
            operation.get(RESTART).set(true);

            ModelNode response = client.execute(operation);
            ModelNode outcome = response.get(OUTCOME);
            assertThat("Restart server failure!", outcome.asString(), is(SUCCESS));
        }

        protected void copyLibraries(String[] expectedJars) {
            copyLibraries(expectedJars, library.getAbsolutePath());
        }

        protected void copyLibraries(String[] expectedJars, String path) {
            // TODO implements
        }

        /**
         * @param expectedJars Expected Jars, the order matter, it will compare order of Jars in the log, null disable check
         */
        protected void checkLogs(String[] expectedJars) {
            // TODO implements
        }

        protected void deployApplication(String appName) {
            // TODO implements
        }

        /**
         * Register global directory
         * Verify the response for success
         *
         * @param name Name of new global directory
         */
        protected ModelNode register(String name) throws IOException {
            return register(name, library.getAbsolutePath(), true);
        }

        /**
         * Register global directory
         *
         * @param name          Name of new global directory
         * @param path
         * @param expectSuccess If is true verify the response for success, If is false only return operation result
         */
        protected ModelNode register(String name, String path, boolean expectSuccess) throws IOException {
            // /subsystem=ee/global-directory=<<name>>:add(path=<<path>>)
            final ModelNode address = new ModelNode();
            address.add(SUBSYSTEM, SUBSYSTEM_EE)
                    .add(GLOBAL_DIRECTORY, name)
                    .protect();
            final ModelNode operation = new ModelNode();
            operation.get(OP).set(ADD);
            operation.get(INCLUDE_RUNTIME).set(true);
            operation.get(OP_ADDR).set(address);
            operation.get(PATH).set(path);

            ModelNode response = client.execute(operation);
            ModelNode outcome = response.get(OUTCOME);
            if (expectSuccess) {
                assertThat("Registration of global directory " + name + " failure!", outcome.asString(), is(SUCCESS));
            }
            return response;
        }

        /**
         * Remove global directory
         *
         * @param name Name of global directory for removing
         */
        protected ModelNode remove(String name) throws IOException {
            // /subsystem=ee/global-directory=<<name>>:remove
            final ModelNode address = new ModelNode();
            address.add(SUBSYSTEM, SUBSYSTEM_EE)
                    .add(GLOBAL_DIRECTORY, name)
                    .protect();
            final ModelNode operation = new ModelNode();
            operation.get(OP).set(REMOVE);
            operation.get(INCLUDE_RUNTIME).set(true);
            operation.get(OP_ADDR).set(address);

            ModelNode response = client.execute(operation);
            ModelNode outcome = response.get(OUTCOME);
            assertThat("Remove of global directory " + name + "  failure!", outcome.asString(), is(SUCCESS));
            return response;
        }

        /**
         * Verify if is global directory is registered and contains right path
         *
         * @param name Name of global directory for verify
         * @param path Expected set path for current global directory
         */
        protected ModelNode verifyProperlyRegistered(String name, String path) throws IOException {
            // /subsystem=ee/global-directory=<<name>>:read-resource
            final ModelNode address = new ModelNode();
            address.add(SUBSYSTEM, SUBSYSTEM_EE)
                    .add(GLOBAL_DIRECTORY, name)
                    .protect();
            final ModelNode operation = new ModelNode();
            operation.get(OP).set(READ_RESOURCE_OPERATION);
            operation.get(INCLUDE_RUNTIME).set(true);
            operation.get(OP_ADDR).set(address);

            ModelNode response = client.execute(operation);
            ModelNode outcome = response.get(OUTCOME);
            assertThat("Read resource of global directory " + name + " failure!", outcome.asString(), is(SUCCESS));

            final ModelNode result = response.get(RESULT);
            assertThat("Global directory " + name + " have set wrong path!", result.get(PATH).asString(), is(path));
            return response;
        }

        /**
         * Verify global directory isn't exist
         *
         * @param name Name of global directory for verify
         */
        protected ModelNode verifyNonExist(String name) throws IOException {
            // /subsystem=ee/global-directory=<<name>>:read-resource
            final ModelNode address = new ModelNode();
            address.add(SUBSYSTEM, SUBSYSTEM_EE)
                    .add(GLOBAL_DIRECTORY, name)
                    .protect();
            final ModelNode operation = new ModelNode();
            operation.get(OP).set(READ_RESOURCE_OPERATION);
            operation.get(INCLUDE_RUNTIME).set(true);
            operation.get(OP_ADDR).set(address);

            ModelNode response = client.execute(operation);
            ModelNode outcome = response.get(OUTCOME);
            assertThat("Global directory " + name + " still exist!", outcome.asString(), not(SUCCESS));
            return response;
        }
    }

    protected static class ClientHolder {

        private final ManagementClient mgmtClient;

        private ClientHolder(ManagementClient mgmtClient) {
            this.mgmtClient = mgmtClient;
        }

        protected static ClientHolder init() {
            final ModelControllerClient client = TestSuiteEnvironment.getModelControllerClient();
            ManagementClient mgmtClient = new ManagementClient(client, TestSuiteEnvironment.getServerAddress(),
                    TestSuiteEnvironment.getServerPort(), "http-remoting");
            return new ClientHolder(mgmtClient);
        }

        /**
         * Execute operation in wildfly
         *
         * @param operation Cli command represent in ModelNode interpretation
         */
        protected ModelNode execute(final ModelNode operation) throws
                IOException {
            return mgmtClient.getControllerClient().execute(operation);
        }

        protected String getWebUri() {
            return mgmtClient.getWebUri().toString();
        }

    }
}
