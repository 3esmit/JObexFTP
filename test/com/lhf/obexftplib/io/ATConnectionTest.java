/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lhf.obexftplib.io;

import com.lhf.obexftplib.etc.OBEXDevice;
import com.lhf.obexftplib.etc.Utility;
import com.lhf.obexftplib.event.ATEventListener;
import com.lhf.obexftplib.event.ConnectionModeListener;
import com.lhf.obexftplib.event.DataEventListener;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import gnu.io.SerialPort;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
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
public class ATConnectionTest {

    private static ArrayList<String> ident = new ArrayList<String>();
    private static CommPortIdentifier selectedTestPort;
    private ATConnection instance = null;
    private static final byte[] AT_TEST_BYTE_ARR = new byte[]{'A', 'T', '\r'};

    public ATConnectionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (System.getProperty("os.name").equalsIgnoreCase("linux")) {
            System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0:/dev/ttyACM1:/dev/ttyACM2:/dev/ttyACM3:/dev/ttyUSB0:/dev/ttyUSB1:/dev/ttyUSB2:/dev/ttyUSB3:/dev/ttyS0:/dev/ttyS1:/dev/ttyS2:/dev/ttyS3");
        }
        Enumeration<CommPortIdentifier> e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier = e.nextElement();
            ident.add(commPortIdentifier.getName());
        }
        selectedTestPort = CommPortIdentifier.getPortIdentifier((String) JOptionPane.showInputDialog(null, "Select CommPort to Test", "TestDrivenDevelopment", 0, null, ident.toArray(), 0));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        try {
            instance = new ATConnection(selectedTestPort);
        } catch (Exception ex) {
            Logger.getLogger(ATConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @After
    public void tearDown() {
        if (instance.getConnMode() != ATConnection.MODE_DISCONNECTED) {
            try {
                instance.setConnMode(ATConnection.MODE_DISCONNECTED);
            } catch (Exception ex) {
                Logger.getLogger(ATConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Test of send method, of class JObexProtocol.
     */
    @Test
    public void testSend() throws Exception {
        instance.setConnMode(ATConnection.MODE_AT);

        byte[] result = instance.send(AT_TEST_BYTE_ARR, 30);
        assertTrue(result.length > 0);
        instance.setConnMode(ATConnection.MODE_DISCONNECTED);

    }


    /**
     * Test of setConnMode method, of class ATConnection.
     */
    @Test
    public void testSetConnMode() throws Exception {
        int newConnMode = 0;
        instance.setConnMode(newConnMode);
        assertEquals(newConnMode, instance.getConnMode());

        newConnMode = 1;
        instance.setConnMode(newConnMode);
        assertEquals(newConnMode, instance.getConnMode());

        newConnMode = 2;
        instance.setConnMode(newConnMode);
        assertEquals(newConnMode, instance.getConnMode());
    }

    /**
     * Test of getConnMode method, of class ATConnection.
     */
    @Test
    public void testGetConnMode() {
        int expResult = ATConnection.MODE_DISCONNECTED;
        int result = instance.getConnMode();
        assertEquals(expResult, result);
    }

    /**
     * Test of identifyDevice method, of class ATConnection.
     */
    @Test
    public void testIdentifyDevice() throws Exception {
        instance.identifyDevice();
        assertNull(instance.getDevice());
        instance.setConnMode(ATConnection.MODE_DATA);
        assertNotNull(instance.getDevice());
    }

    /**
     * Test of addConnectionModeListener method, of class ATConnection.
     */
    @Test
    public void testAddConnectionModeListener() {
    }

    /**
     * Test of removeConnectionModeListener method, of class ATConnection.
     */
    @Test
    public void testRemoveConnectionModeListener() {
    }

    /**
     * Test of addDataEventListener method, of class ATConnection.
     */
    @Test
    public void testAddDataEventListener() {
    }

    /**
     * Test of addATEventListener method, of class ATConnection.
     */
    @Test
    public void testAddATEventListener() {
    }

    /**
     * Test of removeDataEventListener method, of class ATConnection.
     */
    @Test
    public void testRemoveDataEventListener() {
    }

    /**
     * Test of removeATEventListener method, of class ATConnection.
     */
    @Test
    public void testRemoveATEventListener() {
    }

    /**
     * Test of getFlowControl method, of class ATConnection.
     */
    @Test
    public void testGetFlowControl() {
        byte result = instance.getFlowControl();
        assertEquals(ATConnection.FLOW_RTSCTS, result);
    }

    /**
     * Test of getDevice method, of class ATConnection.
     */
    @Test
    public void testGetDevice() throws IOException {
        OBEXDevice result = instance.getDevice();
        assertNull(result);
        instance.setConnMode(ATConnection.MODE_AT);
        assertNotNull(instance.getDevice());
    }

    /**
     * Test of setDevice method, of class ATConnection.
     */
    @Test
    public void testSetDevice() throws IOException {
        instance.setDevice(null);
        assertNull(instance.getDevice());
        instance.setConnMode(ATConnection.MODE_AT);
        assertNotNull(instance.getDevice());
        instance.setDevice(OBEXDevice.DEFAULT);
        assertEquals(OBEXDevice.DEFAULT, instance.getDevice());
        instance.setDevice(OBEXDevice.TC65);
        assertEquals(OBEXDevice.TC65, instance.getDevice());

    }
}
