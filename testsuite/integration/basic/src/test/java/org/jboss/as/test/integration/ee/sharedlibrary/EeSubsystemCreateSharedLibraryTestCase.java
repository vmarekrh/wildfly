package org.jboss.as.test.integration.ee.sharedlibrary;

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
    ===== Scenario 1

    org.jboss.as.test.integration.ee.sharedlibrary.EeSubsystemCreateSharedLibraryTestCase#testSingleCorrectLib

    . Test prerequisites
    .. Create temporary directory and include test jars dependency of test deployment application
    . Define shared library by CLI command
    . Check if shared library exist are registered properly
    . Restart server
    . Check if shared library is registered properly and check generated module.xml file if contains all jars with alphabetical order
    . Add into new test jar into shared library directory
    . Restart server
    . Verify generated module.xml file if contains new jar and is its jars definition are in alphabetical order
    . Deploy test application deployment
    . Call some method from shared library in deployment and verify method output
*/
    @Test
    public void testSingleCorrectLib() {

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
/*
===== Scenario 3

    org.jboss.as.test.integration.ee.sharedlibrary.EeSubsystemCreateSharedLibraryTestCase#testJBossModulesMissingDependencyInSharedLibs

    . Test prerequisites
    .. Create temporary directory and include test jars, that are not used in deployment
    . Define shared library by CLI command
    . Check if shared library exist are registered properly
    . Restart server
    . Check if shared library is registered properly and check generated module.xml file if contains all jars with alphabetical order
    . Deploy test application deployment
    . Call some method in deployment from library that is not a part of shared library, deployment and any other server module. Verify JBoss modules throw correct error message about missing dependency
*/

    @Test
    public void testJBossModulesMissingDependencyInSharedLibs() {

    }

/*
===== Scenario 4

    org.jboss.as.test.integration.ee.sharedlibrary.EeSubsystemCreateSharedLibraryTestCase#testJBossModulesFoundCorruptedJarInSharedLibs

    . Test prerequisites
    .. Create temporary directory and include corrupted test jar
    . Define shared library by CLI command
    . Check if shared library exist are registered properly
    . Restart server
    . Check if shared library is registered properly and check generated module.xml file if contains all jars with alphabetical order
    . Deploy test application deployment
    . Call some method from shared library in deployment and verify JBoss modules throw correct error message about corrupted file
*/

    @Test
    public void testJBossModulesFoundCorruptedJarInSharedLibs() {

    }

/*
===== Scenario 5

    org.jboss.as.test.integration.ee.sharedlibrary.EeSubsystemCreateSharedLibraryTestCase#testJBossModulesFoundDuplicitesInSharedLibs

    . Test prerequisites
    .. Create temporary directory and include test jars with duplicities dependency of test deployment application
    . Define shared library by CLI command
    . Check if shared library exist are registered properly
    . Restart server
    . Check if shared library is registered properly and check generated module.xml file if contains all jars with alphabetical order
    . Deploy test application deployment
    . Call some method from shared library in deployment and verify JBoss modules throw correct error message about duplicities jar files
*/

    @Test
    public void testJBossModulesFoundDuplicitesInSharedLibs() {

    }

}
