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
import javax.swing.JOptionPane;
import com.lhf.obexftplib.fs.OBEXFile;
import com.lhf.obexftplib.fs.OBEXFolder;
import com.lhf.obexftplib.fs.OBEXObject;
import java.util.Iterator;
import java.util.Map;
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
        Utility.configLogger(Level.FINEST);
        selectedTestPort = CommPortIdentifier.getPortIdentifier((String) JOptionPane.showInputDialog(null, "WARNING:\nThis test is going to\nerase all the contents\nof the following device:\n", "TDD: Select CommPort to Test", 0, null, IDENT.toArray(), 0));
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
        System.out.println("Simple Connect/Disconnect test");
        boolean b1 = instance.connect();
        boolean b2 = instance.disconnect();
        assertTrue(b1);
        assertTrue(b2);
    }

    /**
     * Test of abort method, of class OBEXClient.
     */
    @Test
    public void testAbort() throws Exception {
        //this method is buggy to test
    }

    @Test
    public void testCreateDir() throws IOException {
        byte[] rootListing, testFolderListing;
        instance.connect();
        //try to go to an unexistentfolder, and must return the root listing
        rootListing = instance.loadFolderListing().getContents();
        //move to test folder and list
        boolean b2 = instance.changeDirectory(TEST_FOLDER.getPath(), true);
        testFolderListing = instance.loadFolderListing().getContents();
        instance.disconnect();
        assertTrue(b2);
        assertFalse(Utility.compareBytes(rootListing, testFolderListing));
    }

    /**
     * Test of changeDirectory method, of class OBEXClient.
     */
    @Test
    public void testChangeDirectory() throws Exception {
        byte[] rootListing, rootListing2, testFolderListing, testFolderListing3, rootListing3, testFolderListing2, rootListing4;

        //connect and list default (root) folder.
        instance.connect();
        rootListing = instance.loadFolderListing().getContents();

        //try to go to an unexistentfolder, and must return the root listing
        boolean b1 = instance.changeDirectory("unExistentFolder", false);
        rootListing2 = instance.loadFolderListing().getContents();

        //move to test folder and list
        boolean b2 = instance.changeDirectory(TEST_FOLDER.getPath(), false);
        testFolderListing = instance.loadFolderListing().getContents();

        //try to go to an unexistentfolder, and must return the testfolder listing
        boolean b3 = instance.changeDirectory("unExistentFolder", false);
        testFolderListing2 = instance.loadFolderListing().getContents();

        //move up and list
        boolean b4 = instance.changeDirectory(TEST_FOLDER.getParentFolder().getPath(), false);
        rootListing3 = instance.loadFolderListing().getContents();

        //move to test folder and list
        boolean b5 = instance.changeDirectory(TEST_FOLDER.getPath(), false);
        testFolderListing3 = instance.loadFolderListing().getContents();

        //move to parent and list.
        boolean b6 = instance.changeDirectory(TEST_FOLDER.getParentFolder().getPath(), false);
        rootListing4 = instance.loadFolderListing().getContents();

        instance.disconnect();

        assertFalse(b1);
        assertTrue(b2);
        assertFalse(b3);
        assertTrue(b4);
        assertTrue(b5);
        assertTrue(b6);

        assertArrayEquals(rootListing, rootListing2);
        assertArrayEquals(rootListing, rootListing3);
        assertArrayEquals(rootListing, rootListing4);
        assertArrayEquals(testFolderListing, testFolderListing2);
        assertArrayEquals(testFolderListing, testFolderListing3);
    }

    /**
     * Test of removeFile method, of class OBEXClient.
     */
    @Test
    public void testRemoveObject() throws Exception {
        System.out.println("Testing write/remove of object");
        instance.connect();
        final OBEXFile removeFile = createFile(1024 * 10, "remove.tst", TEST_FOLDER);
        instance.changeDirectory(TEST_FOLDER.getPath(), true);
        instance.writeFile(removeFile);
        instance.removeObject(removeFile);
        instance.disconnect();
    }

    /**
     * Test of removeFile method, of class OBEXClient.
     */
    @Test
    public void testCreateRemoveFolder() throws Exception {
        System.out.println("Testing create/remove of folder");
        instance.connect();
        OBEXFolder folder = new OBEXFolder("folderToDelete");
        boolean bb1 = instance.changeDirectory(folder.getPath(), true);
        OBEXFolder list1 = instance.loadFolderListing();
        boolean b1 = list1.getChildFolder(folder.getName()) != null;

        boolean bb2 = instance.changeDirectory(folder.getParentFolder().getPath(), true);
        OBEXFolder list2 = instance.loadFolderListing();
        boolean b2 = list2.getChildFolder(folder.getName()) != null;

        boolean bb3 = instance.removeObject(folder);
        OBEXFolder list3 = instance.loadFolderListing();

        boolean b3 = list3.getChildFolder(folder.getName()) != null;
        instance.disconnect();

        System.out.println(list1);
        System.out.println(list2);
        System.out.println(list3);
        assertTrue(bb1);
        assertTrue(bb2);
        assertTrue(bb3);
        assertFalse(b1);
        assertTrue(b2);
        assertFalse(b3);
    }

    /**
     * Test of readFile method, of class OBEXClient.
     */
    @Test
    public void testReadWriteFile() throws Exception {
        final OBEXFile writeFile = createFile(110132, "readWrite.tst", TEST_FOLDER);

        instance.connect();
        instance.changeDirectory(TEST_FOLDER.getPath(), false);
        instance.writeFile(writeFile);
        OBEXFile readFile = instance.readFile(writeFile.getName());
        instance.removeObject(readFile);
        instance.disconnect();

        assertArrayEquals(writeFile.getContents(), readFile.getContents());
    }

    /**
     * Test of listFolder method, of class OBEXClient.
     */
    @Test
    public void testListFolder() throws Exception {
        boolean[] testList1 = new boolean[10];
        instance.connect();
        instance.changeDirectory(TEST_FOLDER.getPath(), false);
        for (int i = 0; i < testList1.length; i++) {
            testList1[i] = instance.writeFile(createFile(1, "testList" + i + ".tst", TEST_FOLDER));
        }
        Map<String, OBEXObject> objs = instance.loadFolderListing().getSubobjects();
        instance.disconnect();

        int i = 0;
        for (Iterator<OBEXObject> it = objs.values().iterator(); it.hasNext();) {
            OBEXObject obj = it.next();
            assertTrue(obj.getName(), obj.getName().equals("testList" + i + ".tst"));
            i++;
        }
        assertTrue(i > 0);

    }

    /**
     * Test of getMaxPacketLenght method, of class OBEXClient.
     */
    @Test
    public void testGetMaxPacketLenght() {
        //no need for testing
    }

    /**
     * Test of setMaxPacketLenght method, of class OBEXClient.
     */
    @Test
    public void testSetMaxPacketLenght() {
        //no need for testing
    }

    /**
     * Test of eraseDisk method, of class OBEXClient.
     */
    @Test
    public void testEraseDisk() throws Exception {
        instance.connect();
//        boolean b = instance.eraseDisk();
        String listing = instance.loadFolderListing().getListing();
        instance.disconnect();
//        assertTrue(b);
        System.out.println(listing);

    }

    private static OBEXFile createFile(final int size, final String filename, OBEXFolder folder) throws IOException {
        if (folder == null) {
            folder = OBEXFile.ROOT_FOLDER;
        }
        StringBuilder builder = new StringBuilder();
        if (size > 3) {
            while (builder.length() < size - 3) {
                for (int i = 1; i < 10; i++) {
                    builder.append(i);
                }
            }
        }
        builder.append("END");

        OBEXFile file = new OBEXFile(folder, filename);
        file.setContents(builder.toString().getBytes());

        return file;
    }
}
