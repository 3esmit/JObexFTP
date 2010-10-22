/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lhf.obexftplib.io;

import com.lhf.obexftplib.etc.Utility;
import gnu.io.CommPortIdentifier;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import com.lhf.obexftplib.fs.OBEXFile;
import com.lhf.obexftplib.fs.OBEXFolder;
import com.lhf.obexftplib.fs.OBEXObject;
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
public class OBEXClientTest {

    private static final ArrayList<String> IDENT = new ArrayList<String>();
    public static final OBEXFolder TEST_FOLDER = OBEXObject.ROOT_FOLDER.addFolder("tests");
    private static CommPortIdentifier selectedTestPort;
    private static ATConnection conn = null;
    private OBEXClient instance = new OBEXClient(conn);

    public OBEXClientTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        if (System.getProperty("os.name").equalsIgnoreCase("linux")) {
            System.setProperty("gnu.io.rxtx.SerialPorts", "/dev/ttyACM0:/dev/ttyACM1:/dev/ttyACM2:/dev/ttyACM3:/dev/ttyUSB0:/dev/ttyUSB1:/dev/ttyUSB2:/dev/ttyUSB3:/dev/ttyS0:/dev/ttyS1:/dev/ttyS2:/dev/ttyS3");
        }
        Enumeration<CommPortIdentifier> e = CommPortIdentifier.getPortIdentifiers();
        while (e.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier = e.nextElement();
            IDENT.add(commPortIdentifier.getName());
        }
        selectedTestPort = CommPortIdentifier.getPortIdentifier((String) JOptionPane.showInputDialog(null, "Select CommPort to Test", "TestDrivenDevelopment", 0, null, IDENT.toArray(), 0));
        conn = new ATConnection(selectedTestPort);
        conn.setConnMode(ATConnection.MODE_DATA);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        conn.setConnMode(ATConnection.MODE_DISCONNECTED);
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of connect method, of class OBEXClient.
     */
    @Test
    public void testConnect() throws Exception {
        boolean b = instance.connect();
        instance.disconnect();
        assertTrue(b);
    }

    /**
     * Test of disconnect method, of class OBEXClient.
     */
    @Test
    public void testDisconnect() throws Exception {
        instance.connect();
        boolean b = instance.disconnect();
        assertTrue(b);
    }

    /**
     * Test of abort method, of class OBEXClient.
     */
    @Test
    public void testAbort() throws Exception {
        instance.connect();
        final OBEXFile abortFile = createFile(1024 * 10, "at1.tst", TEST_FOLDER);
        boolean b = instance.abort();
        instance.changeDirectory(TEST_FOLDER.getPath(), true);
        instance.removeObject(abortFile);
        instance.disconnect();
//        new Thread() {
//
//            @Override
//            public void run() {
//                try {
//                    synchronized (TEST_FOLDER) {
//                        TEST_FOLDER.notifyAll();
//                        instance.writeFile(abortFile);
//                        Thread.sleep(1000);
//                    }
//                } catch (InterruptedException ex) {
//                    Logger.getLogger(OBEXClientTest.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (IOException ex) {
//                    Logger.getLogger(OBEXClientTest.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }.start();
//        boolean b2;
//        synchronized (TEST_FOLDER) {
//            TEST_FOLDER.wait(10000);
//            Thread.sleep(1000);
//            b2 = instance.abort();
//        }
//        instance.listFolder();
//        instance.disconnect();
//        String listing = TEST_FOLDER.getListing();
//        System.out.println(listing);
//        assertTrue(b);
//        assertTrue(b2);
//        assertFalse(listing.indexOf(abortFile.getName()) > 0);
    }

    /**
     * Test of eraseDisk method, of class OBEXClient.
     */
    @Test
    public void testEraseDisk() throws Exception {
//        instance.connect();
        //
//        instance.disconnect();
    }

    /**
     * Test of removeFile method, of class OBEXClient.
     */
    @Test
    public void testRemoveFile() throws Exception {
        instance.connect();
        final OBEXFile abortFile = createFile(1024 * 10, "at1.tst", TEST_FOLDER);
        instance.changeDirectory(TEST_FOLDER.getPath(), true);
        instance.removeObject(abortFile);
        instance.disconnect();
    }

    /**
     * Test of changeDirectory method, of class OBEXClient.
     */
    @Test
    public void testChangeDirectory() throws Exception {
        instance.connect();
//        boolean b = instance.changeDirectory("nonExistentDir", false);
        boolean b2 = instance.changeDirectory(TEST_FOLDER.getPath(), false);
//        boolean b3 = instance.changeDirectory("createdDir", true);
//        boolean b4 = instance.changeDirectory(TEST_FOLDER.getPath(), true);
        instance.listFolder();
        String listing = TEST_FOLDER.getListing();
//        System.out.println(listing);
        boolean b5 = (listing.indexOf("createdDir") > 0);

        instance.removeObject(Utility.nameToBytes("createdDir"));

        instance.listFolder();
        listing = TEST_FOLDER.getListing();
        System.out.println(listing);
        boolean b6 = (listing.indexOf("createdDir") > 0);
        instance.disconnect();

//        assertFalse(b);
        assertTrue(b2);
//        assertTrue(b3);
//        assertTrue(b4);
        assertTrue(b5);
        assertFalse(b6);
    }

    /**
     * Test of readFile method, of class OBEXClient.
     */
    @Test
    public void testReadFile() throws Exception {
//        instance.connect();
        //
//        instance.disconnect();
    }

    /**
     * Test of listFolder method, of class OBEXClient.
     */
    @Test
    public void testListFolder() throws Exception {
    }

    /**
     * Test of writeFile method, of class OBEXClient.
     */
    @Test
    public void testWriteFile() throws IOException {
//        instance.connect();
        //
//        instance.disconnect();
    }

    /**
     * Test of getMaxPacketLenght method, of class OBEXClient.
     */
    @Test
    public void testGetMaxPacketLenght() {
    }

    /**
     * Test of setMaxPacketLenght method, of class OBEXClient.
     */
    @Test
    public void testSetMaxPacketLenght() {
    }

    private static OBEXFile createFile(final int size, final String filename, OBEXFolder folder) throws IOException {
        if (folder == null) {
            folder = OBEXFile.ROOT_FOLDER;
        }
        StringBuilder builder = new StringBuilder();
        while (builder.length() < size) {
            for (int i = 1; i < 10; i++) {
                builder.append(i);
            }
        }
        OBEXFile file = new OBEXFile(folder, filename);
        file.setContents(builder.toString().getBytes());

        return file;
    }
}
