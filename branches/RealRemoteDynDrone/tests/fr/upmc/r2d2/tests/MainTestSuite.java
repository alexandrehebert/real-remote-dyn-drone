/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.upmc.r2d2.tests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author alexandre
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({MainTests.class})
public class MainTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {}

    @AfterClass
    public static void tearDownClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}
    
    @Test
    public void runTests() throws Exception {
        MainTests.main();
    }
    
}
