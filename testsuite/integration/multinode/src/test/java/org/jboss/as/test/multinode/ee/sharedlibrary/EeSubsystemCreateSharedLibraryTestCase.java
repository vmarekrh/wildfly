package org.jboss.as.test.multinode.ee.sharedlibrary;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Vratislav Marek (vmarek@redhat.com)
 **/
public class EeSubsystemCreateSharedLibraryTestCase {


    @Before
    public void setup() {
    }


    /*
    ===== Scenario 2

        org.jboss.as.test.integration.ee.sharedlibrary.EeSubsystemCreateSharedLibraryTestCase#testMultipleCorrectLib

                . Test prerequisites
    .. Create 2 temporary directory and include selected test jars dependency of test deployment application
    . Define 2 shared library by CLI command
                . Check if every 2 shared library are registered properly
                . Restart server
    . Check if all shared library are registered properly and check generated module.xml files if contains all jars with alphabetical order
    . Deploy 2 test application deployment
    . Call some method from shared library in deployment and verify method output on every application deployment
    */

    @Test
    public void testMultipleCorrectLib() {

    }

}
