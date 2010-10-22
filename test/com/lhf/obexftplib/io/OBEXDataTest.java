/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lhf.obexftplib.io;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ricardo
 */
public class OBEXDataTest {

    public OBEXDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getPacketLenght method, of class OBEXData.
     */
    @Test
    public void testGetPacketLenght() {
        System.out.println("getPacketLenght");
        OBEXData instance = new OBEXData();
        int expResult = 0;
        int result = instance.getPacketLenght();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDataLenght method, of class OBEXData.
     */
    @Test
    public void testGetDataLenght() {
        System.out.println("getDataLenght");
        OBEXData instance = new OBEXData();
        int expResult = 0;
        int result = instance.getDataLenght();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pushData method, of class OBEXData.
     */
    @Test
    public void testPushData() {
        System.out.println("pushData");
        byte[] newData = null;
        OBEXData instance = new OBEXData();
        instance.pushData(newData);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isReady method, of class OBEXData.
     */
    @Test
    public void testIsReady() {
        System.out.println("isReady");
        OBEXData instance = new OBEXData();
        boolean expResult = false;
        boolean result = instance.isReady();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pullData method, of class OBEXData.
     */
    @Test
    public void testPullData() {
        System.out.println("pullData");
        OBEXData instance = new OBEXData();
        byte[] expResult = null;
        byte[] result = instance.pullData();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}