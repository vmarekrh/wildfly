package org.jboss.as.test.integration.domain.ee.sharedlibrary;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Vratislav Marek (vmarek@redhat.com)
 **/
public class EeSubsystemCreateSharedLibraryDomainTestCase {

    @BeforeClass
    public static void setupDomain() {
    }

    @Before
    public void set() {
    }

    @AfterClass
    public static void tearDownDomain() {

    }

    /*
    org.jboss.as.test.integration.domain.ee.sharedlibrary.EeSubsystemCreateSharedLibraryDomainTestCase#testAppSharedLib

    . Test prerequisites
    .. Create temporary directory and include test jars dependency of test deployment application to all domain servers
    . Define shared library by CLI command
    . Check if shared library is registered properly
    . Restart domain
    . Check if shared library is registered properly and check generated module.xml file if contains all jars with alphabetical order
    . Add into new test jar into shared library directory
    . Restart domain
    . Verify generated module.xml file if contains new jar and is its jars definition are in alphabetical order on all domain servers
    . Deploy test application deployment
    . Call some method from shared library in deployment and verify method output
* */

    @Test
    public void testAppSharedLib() {

    }
}
