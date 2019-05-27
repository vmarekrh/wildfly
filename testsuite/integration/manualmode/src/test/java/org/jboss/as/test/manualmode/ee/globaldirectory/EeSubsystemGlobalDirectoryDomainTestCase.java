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
import org.jboss.as.test.shared.TestSuiteEnvironment;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.PATH;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;
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

    protected static final Logger LOGGER = Logger.getLogger(EeSubsystemGlobalDirectoryDomainTestCase.class);

    @ArquillianResource
    protected static ContainerController containerController;

    @ArquillianResource
    protected static Deployer deployer;

    protected ClientHolder client;

//    @BeforeClass
//    public static void beforeClass() {
//    }

    @Before
    public void before() {
        if (!containerController.isStarted(CONTAINER)) {
            containerController.start(CONTAINER);
            this.client = ClientHolder.init();
        }
    }

    @After
    public void after() {
        if (containerController.isStarted(CONTAINER)) {
            containerController.stop(CONTAINER);
            this.client = null;
        }
    }

//    @AfterClass
//    public static void afterClass() {
//
//    }
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
        // /subsystem=ee/global-directory=<<name>>:read-resource
        final ModelNode address = new ModelNode();
        address.add(SUBSYSTEM, SUBSYSTEM_EE)
                .add(GLOBAL_DIRECTORY, "test1")
                .protect();
        final ModelNode operation = new ModelNode();
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(INCLUDE_RUNTIME).set(true);
        operation.get(OP_ADDR).set(address);

        ModelNode response = client.execute(operation);
//        ModelNode outcome = response.get(OUTCOME);
//        assertThat("Read resource of global directory " + name + " failure!", outcome.asString(), is(SUCCESS));
//
//        final ModelNode result = response.get(RESULT);
//        assertThat("Global directory " + name + " have set wrong path!", result.get(PATH).asString(), is(path));
        //return response;


    }


    protected static class ClientHolder {

        private final ManagementClient mgmtClient;

        private ClientHolder(ManagementClient mgmtClient) {
            this.mgmtClient = mgmtClient;
        }

        public static ClientHolder init() {
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
        public ModelNode execute(final ModelNode operation) throws
                IOException {
            return mgmtClient.getControllerClient().execute(operation);
        }

        public String getWebUri() {
            return mgmtClient.getWebUri().toString();
        }

    }


//    protected static class ServerHolder {
//        private final String name;
//        private final String host;
//        private final int portOffset;
//        private volatile ModelControllerClient client;
//        private volatile CommandContext commandCtx;
//        private volatile ByteArrayOutputStream consoleOut = new ByteArrayOutputStream();
//        private final List<String> deployments = new ArrayList<>();
//        private String jbossServerConfigDir;
//
//        private volatile String snapshot;
//
//        public ServerHolder(String name, String host) {
//            this(name, host, 0);
//        }
//
//        public ServerHolder(String name, String host, int portOffset) {
//            this.name = name;
//            this.host = host;
//            this.portOffset = portOffset;
//        }
//
//        public void resetContainerConfiguration(AbstractSecurityContextPropagationTestBase.ServerConfiguration config)
//                throws CommandLineException, IOException, MgmtOperationException {
//            if (!containerController.isStarted(name)) {
//                containerController.start(name);
//                client = ModelControllerClient.Factory.create(host, getManagementPort());
//                commandCtx = CLITestUtil.getCommandContext(host, getManagementPort(), null, consoleOut, -1);
//                commandCtx.connectController();
//                readSnapshot();
//                jbossServerConfigDir = readJbossServerConfigDir();
//
//                if (snapshot == null) {
//                    // configure each server just once
//                    takeSnapshot();
//                    createPropertyFile(config.getAdditionalUsers());
//                    final File cliFIle = File.createTempFile("seccontext-", ".cli");
//                    try (FileOutputStream fos = new FileOutputStream(cliFIle)) {
//                        IOUtils.copy(
//                                AbstractSecurityContextPropagationTestBase.class.getResourceAsStream("seccontext-setup.cli"),
//                                fos);
//                    }
//                    addCliCommands(cliFIle, config.getCliCommands());
//                    runBatch(cliFIle);
//                    switchJGroupsToTcpping();
//                    cliFIle.delete();
//                    reload();
//
//                    deployments.addAll(config.getDeployments());
//                    for (String deployment : config.getDeployments()) {
//                        deployer.deploy(deployment);
//                    }
//                }
//            }
//        }
//
//        public void shutDown() throws IOException {
//            if (containerController.isStarted(name)) {
//                // deployer.undeploy(name);
//                for (String deployment : deployments) {
//                    deployer.undeploy(deployment);
//                }
//                commandCtx.terminateSession();
//                client.close();
//                containerController.stop(name);
//                reloadFromSnapshot();
//                Files.deleteIfExists(propertyFile);
//            }
//        }
//
//        public int getManagementPort() {
//            return 9990 + portOffset;
//        }
//
//        public int getApplicationPort() {
//            return 8080 + portOffset;
//        }
//
//        public String getApplicationHttpUrl() throws IOException {
//            return "http://" + NetworkUtils.formatPossibleIpv6Address(host) + ":" + getApplicationPort();
//        }
//
//        public String getApplicationRemotingUrl() throws IOException {
//            return "remote+" + getApplicationHttpUrl();
//        }
//
//        /**
//         * Sends command line to CLI.
//         *
//         * @param line        specifies the command line.
//         * @param ignoreError if set to false, asserts that handling the line did not result in a
//         *                    {@link org.jboss.as.cli.CommandLineException}.
//         * @return true if the CLI is in a non-error state following handling the line
//         */
//        public boolean sendLine(String line, boolean ignoreError) {
//            consoleOut.reset();
//            if (ignoreError) {
//                commandCtx.handleSafe(line);
//                return commandCtx.getExitCode() == 0;
//            } else {
//                try {
//                    commandCtx.handle(line);
//                } catch (CommandLineException e) {
//                    StringWriter stackTrace = new StringWriter();
//                    e.printStackTrace(new PrintWriter(stackTrace));
//                    Assert.fail(String.format("Failed to execute line '%s'%n%s", line, stackTrace.toString()));
//                }
//            }
//            return true;
//        }
//
//        /**
//         * Runs given CLI script file as a batch.
//         *
//         * @param batchFile CLI file to run in batch
//         * @return true if CLI returns Success
//         */
//        public boolean runBatch(File batchFile) throws IOException {
//            sendLine("run-batch --file=\"" + batchFile.getAbsolutePath() + "\" -v", false);
//            if (consoleOut.size() <= 0) {
//                return false;
//            }
//            return new CLIOpResult(ModelNode.fromStream(new ByteArrayInputStream(consoleOut.toByteArray())))
//                    .isIsOutcomeSuccess();
//        }
//
//        /**
//         * Switch JGroups subsystem (if present) from using UDP multicast to TCPPING discovery protocol.
//         */
//        private void switchJGroupsToTcpping() throws IOException {
//            consoleOut.reset();
//            try {
//                commandCtx.handle("if outcome==success of /subsystem=jgroups:read-resource()");
//                // TODO This command is deprecated
//                commandCtx.handle(String.format(
//                        "/subsystem=jgroups/stack=tcp/protocol=TCPPING:add(add-index=0, properties={initial_hosts=\"%1$s[7600],%1$s[9600]\",port_range=0})",
//                        Utils.stripSquareBrackets(host)));
//                commandCtx.handle("/subsystem=jgroups/stack=tcp/protocol=MPING:remove");
//                commandCtx.handle("/subsystem=jgroups/channel=ee:write-attribute(name=stack,value=tcp)");
//                commandCtx.handle("end-if");
//            } catch (CommandLineException e) {
//                LOGGER.error("Command line error occured during JGroups reconfiguration", e);
//            } finally {
//                LOGGER.debugf("Output of JGroups reconfiguration (switch to TCPPING): %s",
//                        new String(consoleOut.toByteArray(), StandardCharsets.UTF_8));
//            }
//        }
//
//        private void takeSnapshot() throws IOException, MgmtOperationException {
//            DomainTestUtils.executeForResult(Util.createOperation("take-snapshot", null), client);
//            readSnapshot();
//        }
//
//        private void readSnapshot() throws IOException, MgmtOperationException {
//            ModelNode namesNode = DomainTestUtils.executeForResult(Util.createOperation("list-snapshots", null), client)
//                    .get("names");
//            if (namesNode == null || namesNode.getType() != ModelType.LIST) {
//                throw new IllegalStateException("Unexpected return value from :list-snaphot operation: " + namesNode);
//            }
//            List<ModelNode> snapshots = namesNode.asList();
//            if (!snapshots.isEmpty()) {
//                snapshot = namesNode.get(snapshots.size() - 1).asString();
//            }
//        }
//
//        private void reloadFromSnapshot() throws IOException {
//            if (snapshot != null) {
//                File snapshotFile = new File(jbossServerConfigDir + File.separator + "standalone_xml_history" + File.separator
//                        + "snapshot" + File.separator + snapshot);
//                String standaloneName = snapshot.replaceAll("\\d", "").replaceFirst("-", "");
//                File standaloneFile = new File(jbossServerConfigDir + File.separator + standaloneName);
//                Files.copy(snapshotFile.toPath(), standaloneFile.toPath(), REPLACE_EXISTING);
//                snapshotFile.delete();
//            }
//        }
//
//        private void reload() {
//            ModelNode operation = Util.createOperation("reload", null);
//            ServerReload.executeReloadAndWaitForCompletion(client, operation, (int) SECONDS.toMillis(90), host,
//                    getManagementPort());
//        }
//    }
}
