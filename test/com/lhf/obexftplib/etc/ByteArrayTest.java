/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lhf.obexftplib.etc;

import com.lhf.obexftplib.etc.ByteArray;
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
public class ByteArrayTest {

    public ByteArrayTest() {
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
     * Test of getBytes method, of class ByteArray.
     */
    @Test
    public void testGetBytes() {
        System.out.println("getBytes");
        ByteArray instance = null;
        byte[] expResult = null;
        byte[] result = instance.getBytes();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of length method, of class ByteArray.
     */
    @Test
    public void testLength() {
        System.out.println("length");
        ByteArray instance = null;
        int expResult = 0;
        int result = instance.length();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}