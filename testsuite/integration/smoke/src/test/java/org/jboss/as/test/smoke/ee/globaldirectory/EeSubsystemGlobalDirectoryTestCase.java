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
package org.jboss.as.test.smoke.ee.globaldirectory;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.arquillian.api.ContainerResource;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.ClientConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.INCLUDE_RUNTIME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_RESOURCE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.RESULT;

/**
 * @author Vratislav Marek (vmarek@redhat.com)
 **/
@RunWith(Arquillian.class)
@RunAsClient
public class EeSubsystemGlobalDirectoryTestCase {

    private static final String FIRST_GLOBAL_DIRECTORY_NAME = "";
    private static final String FIRST_GLOBAL_DIRECTORY_PATH = "";

    private static final String SECOND_GLOBAL_DIRECTORY_NAME = "";
    private static final String SECOND_GLOBAL_DIRECTORY_PATH = "";

    private static Logger LOGGER = Logger.getLogger(EeSubsystemGlobalDirectoryTestCase.class);

    @ContainerResource
    private ManagementClient managementClient;

    /*
    Scenario 1 - exist

    org.jboss.as.test.smoke.ee.globaldirectory.EeSubsystemGlobalDirectoryTestCase#testAddAndRemoveSharedLib

    Define global-directory by CLI command
    Verify if global-directory is registered properly with right arguments values
    Remove global-directory by CLI command
    Check if global-directory not exist
    */
    @Test
    public void testAddAndRemoveSharedLib() throws IOException {
        register(FIRST_GLOBAL_DIRECTORY_NAME, FIRST_GLOBAL_DIRECTORY_PATH);
        verifyProperlyRegistered(FIRST_GLOBAL_DIRECTORY_NAME, FIRST_GLOBAL_DIRECTORY_PATH);
        remove(FIRST_GLOBAL_DIRECTORY_NAME);
        verifyNonExist(FIRST_GLOBAL_DIRECTORY_NAME);

        final ModelNode operation = new ModelNode();
        ModelNode response = execute(operation);
        ModelNode result = response.get(RESULT);

        System.out.println("\n\n\n");
        System.out.println(response);
        System.out.println("\n\n\n");
    }

    /*
    Scenario 2 - only one

    org.jboss.as.test.smoke.ee.globaldirectory.EeSubsystemGlobalDirectoryTestCase#testRejectSecondSharedLib

    Define global-directory by CLI command
    Verify if global-directory is registered properly with right arguments values
    Define second global-directory by CLI command
    verify if is throwed correct error message about rejections second global-directory
    */
    @Test
    public void testRejectSecondSharedLib() {
        register(FIRST_GLOBAL_DIRECTORY_NAME, FIRST_GLOBAL_DIRECTORY_PATH);
        verifyProperlyRegistered(FIRST_GLOBAL_DIRECTORY_NAME, FIRST_GLOBAL_DIRECTORY_PATH);
        register(SECOND_GLOBAL_DIRECTORY_NAME, SECOND_GLOBAL_DIRECTORY_PATH);
        verifyNonExist(SECOND_GLOBAL_DIRECTORY_NAME);
    }

    private void register(String name, String path) {

    }

    private void remove(String name) {

    }

    private void verifyProperlyRegistered(String name, String path) {

    }

    private void verifyNonExist(String name) {
    }


    private ModelNode execute(final ModelNode operation) throws
            IOException {
        final ModelNode result = managementClient.getControllerClient().execute(operation);
        if (result.hasDefined(ClientConstants.OUTCOME) && ClientConstants.SUCCESS.equals(
                result.get(ClientConstants.OUTCOME).asString())) {
            return result;
        } else if (result.hasDefined(ClientConstants.FAILURE_DESCRIPTION)) {
            final String failureDesc = result.get(ClientConstants.FAILURE_DESCRIPTION).toString();
            throw new RuntimeException(failureDesc);
        } else {
            throw new RuntimeException("Operation not successful; outcome = " + result.get(ClientConstants.OUTCOME));
        }
    }

}
