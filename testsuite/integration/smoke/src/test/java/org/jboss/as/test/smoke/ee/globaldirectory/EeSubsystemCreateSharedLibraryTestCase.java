package org.jboss.as.test.smoke.ee.globaldirectory;

import org.jboss.as.test.integration.security.common.Utils;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Vratislav Marek (vmarek@redhat.com)
 **/
public class EeSubsystemCreateSharedLibraryTestCase {


    private static Logger LOGGER = Logger.getLogger(EeSubsystemCreateSharedLibraryTestCase.class);

    @Before
    public void setup() {
    }


    /*
    org.jboss.as.test.manualmode.ee.globaldirectory.EeSubsystemCreateSharedLibraryTestCase#testAddAndRemoveSharedLib

    . Define shared library by CLI command
    . Check if shared library is registered properly
    . Remove shared library by CLI command
    . Check if shared library not exist
*/
    @Test
    public void testAddAndRemoveSharedLib() {

    }

}
