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

import org.jboss.logging.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Vratislav Marek (vmarek@redhat.com)
 **/
public class EeSubsystemGlobalDirectoryTestCase {


    private static Logger LOGGER = Logger.getLogger(EeSubsystemGlobalDirectoryTestCase.class);

    @Before
    public void setup() {
    }


    /*
    Scenario 1 - exist

    org.jboss.as.test.smoke.ee.globaldirectory.EeSubsystemGlobalDirectoryTestCase#testAddAndRemoveSharedLib

    Define global-directory by CLI command
    Verify if global-directory is registered properly with right arguments values
    Remove global-directory by CLI command
    Check if global-directory not exist
    */
    @Test
    public void testAddAndRemoveSharedLib() {

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

    }

}
