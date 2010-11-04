/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lhf.obexftplib.etc;

import com.lhf.obexftplib.etc.Utility;
import com.lhf.obexftplib.fs.OBEXFolder;
import com.lhf.obexftplib.io.obexop.AbortResponse;
import com.lhf.obexftplib.io.obexop.ConnectResponse;
import com.lhf.obexftplib.io.obexop.DisconnectResponse;
import com.lhf.obexftplib.io.obexop.GetResponse;
import com.lhf.obexftplib.io.obexop.Header;
import com.lhf.obexftplib.io.obexop.PutResponse;
import com.lhf.obexftplib.io.obexop.Response;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import java.io.ByteArrayInputStream;
import gnu.io.CommPortIdentifier;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public class UtilityTest {

    public UtilityTest() {
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
     * Test of addPortToRxTx method, of class Utilities.
     */
    @Test
    public void testAddPortToRxTx() throws Exception {
        String connPortPath = "/dev/ttyACM0";
        CommPortIdentifier result = Utility.addPortToRxTx(connPortPath);
        assertEquals("/dev/ttyACM0", result.getName());
    }

    /**
     * Test of arrayContainsOK method, of class Utilities.
     */
    @Test
    public void testArrayContainsOK() {
        byte[] b = new byte[]{10, 79, 75, 13};
        boolean expResult = true;
        boolean result = Utility.arrayContainsOK(b);
        assertEquals(expResult, result);

        b = new byte[]{10, 79, 75, 13, 99, 99, 99};
        expResult = true;
        result = Utility.arrayContainsOK(b);
        assertEquals(expResult, result);

        b = new byte[]{99, 99, 99, 10, 79, 75, 13};
        expResult = true;
        result = Utility.arrayContainsOK(b);
        assertEquals(expResult, result);

        b = new byte[]{};
        expResult = false;
        result = Utility.arrayContainsOK(b);
        assertEquals(expResult, result);


        b = new byte[]{10, 79, 79, 13};
        expResult = false;
        result = Utility.arrayContainsOK(b);
        assertEquals(expResult, result);

        b = new byte[]{10, 79, 79, 13};
        expResult = false;
        result = Utility.arrayContainsOK(b);
        assertEquals(expResult, result);
    }

    /**
     * Test of intToBytes method, of class Utility.
     */
    @Test
    public void testIntToBytes() {
        for (int i = 0; i < UtilityBenchMark.NUMBER; i++) {
            assertTrue("intToBytes failed", Utility.compareBytes(
                    UtilityBenchMark.BYTEARRAYS[i],
                    Utility.intToBytes(UtilityBenchMark.INTEGERS[i],
                    UtilityBenchMark.LENGTHS[i])));
        }
    }

    /**
     * Test of bytesToInt method, of class Utility.
     */
    @Test
    public void testBytesToInt() {
        for (int i = 0; i < UtilityBenchMark.NUMBER; i++) {
            assertTrue("bytesToInt failed", Utility.bytesToInt(
                    UtilityBenchMark.BYTEARRAYS[i])
                    == UtilityBenchMark.INTEGERS[i]);
        }
    }

    /**
     * Test of getBytes method, of class Utility.
     */
    @Test
    public void testGetBytes() {
        for (int i = 0; i < HeaderBenchMark.NUMBER; i++) {
            assertTrue("getBytes failed", Utility.compareBytes(
                    HeaderBenchMark.VALUE[i], Utility.getBytes(
                    HeaderBenchMark.RAWDATA[i],
                    HeaderBenchMark.VALUEOFFSET[i],
                    HeaderBenchMark.VALUELENGTH[i])));
        }
    }

    /**
     * Test of setBytes method, of class Utility.
     */
    @Test
    public void testSetBytes() {
        for (int i = 0; i < HeaderBenchMark.NUMBER; i++) {
            byte[] testBytes = new byte[HeaderBenchMark.LENGTH[i]];
            Utility.setBytes(testBytes, HeaderBenchMark.VALUE[i],
                    HeaderBenchMark.VALUEOFFSET[i],
                    HeaderBenchMark.VALUELENGTH[i]);
            assertTrue("setBytes failed",
                    Utility.compareBytes(HeaderBenchMark.VALUE[i],
                    Utility.getBytes(testBytes,
                    HeaderBenchMark.VALUEOFFSET[i],
                    HeaderBenchMark.VALUELENGTH[i])));
        }
    }

    /**
     * Test of byteToHexString method, of class Utility.
     */
    @Test
    public void testByteToHexString() {
        for (int i = 0; i < UtilityBenchMark.NUMBER; i++) {
            assertTrue("byteToHexString failed",
                    UtilityBenchMark.BYTESTRINGS[i].equals(
                    Utility.byteToHexString(UtilityBenchMark.BYTES[i])));
        }
    }

    /**
     * Test of byteArrayListToBytes method, of class Utility.
     */
    @Test
    public void testByteArrayListToBytes() {
    }

    /**
     * Test of dumpBytes method, of class Utility.
     */
    @Test
    public void testDumpBytes() {
    }

    /**
     * Test of compareBytes method, of class Utility.
     */
    @Test
    public void testCompareBytes() {
        /* First, be sure TestUtility.compareBytes works fine */
        for (int i = 0; i < UtilityBenchMark.NUMBER; i++) {
            assertTrue("TestUtility.compareBytes failed",
                    Utility.compareBytes(UtilityBenchMark.BYTEARRAYS[i],
                    UtilityBenchMark.BYTEARRAYS[i]));
            if (i > 0) {
                assertFalse("TestUtility.compareBytes failed",
                        Utility.compareBytes(UtilityBenchMark.BYTEARRAYS[i],
                        UtilityBenchMark.BYTEARRAYS[i - 1]));
            }
        }
    }

    /**
     * Test of nameToBytes method, of class Utility.
     */
    @Test
    public void testNameToBytes() {
        String name = "myName";
        byte[] expResult = {00, 'm', 00, 'y', 00, 'N', 00, 'a', 00, 'm', 00, 'e'};
        byte[] result = Utility.nameToBytes(name);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of removeLastSlash method, of class Utility.
     */
    @Test
    public void testRemoveLastSlash() {
        String path = "a:/asdasd/asdasd/";
        String expResult = "a:/asdasd/asdasd";
        String result = Utility.removeLastSlash(path);
        assertEquals(expResult, result);

        path = "asdasd/asdasd/";
        expResult = "asdasd/asdasd";
        result = Utility.removeLastSlash(path);
        assertEquals(expResult, result);

        path = "../";
        expResult = "..";
        result = Utility.removeLastSlash(path);
        assertEquals(expResult, result);

        path = "../..";
        expResult = "../..";
        result = Utility.removeLastSlash(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of preparePath method, of class Utility.
     */
    @Test
    public void testPreparePath() {
        String path = "\\test\\test\\";
        String expResult = "a:/test/test";
        String result = Utility.preparePath(path);
        assertEquals(expResult, result);

        path = "test\\test\\";
        expResult = "test/test";
        result = Utility.preparePath(path);
        assertEquals(expResult, result);

        path = "/test/test/";
        expResult = "a:/test/test";
        result = Utility.preparePath(path);
        assertEquals(expResult, result);

        path = "a:/test/test/";
        expResult = "a:/test/test";
        result = Utility.preparePath(path);
        assertEquals(expResult, result);

        path = "test\\test";
        expResult = "test/test";
        result = Utility.preparePath(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of getRelativePath method, of class Utility.
     */
    @Test
    public void testGetRelativePath() {
        String newAbsolPath = "a:/f1";
        String actualFolder = "a:/f2";
        String expResult = "../f1";
        String result = Utility.getRelativePath(newAbsolPath, actualFolder);
        assertEquals(expResult, result);

        newAbsolPath = "a:/";
        actualFolder = "a:/";
        expResult = "";
        result = Utility.getRelativePath(newAbsolPath, actualFolder);
        assertEquals(expResult, result);

        newAbsolPath = "a:/f1/f2/f3";
        actualFolder = "a:/f1/f2/f3/f4";
        expResult = "..";
        result = Utility.getRelativePath(newAbsolPath, actualFolder);
        assertEquals(expResult, result);

        newAbsolPath = "a:/f1/f2/f3/f4";
        actualFolder = "a:/f1/f2/f3";
        expResult = "f4";
        result = Utility.getRelativePath(newAbsolPath, actualFolder);
        assertEquals(expResult, result);

        newAbsolPath = "a:/f1/f5/f3/f4";
        actualFolder = "a:/f1/f2/f3";
        expResult = "../../f5/f3/f4";
        result = Utility.getRelativePath(newAbsolPath, actualFolder);
        assertEquals(expResult, result);

        newAbsolPath = "a:/f1/f1/f1/f1";
        actualFolder = "a:/f1/f1/f1";
        expResult = "f1";
        result = Utility.getRelativePath(newAbsolPath, actualFolder);
        assertEquals(expResult, result);

        newAbsolPath = "a:/f1/f1/f1";
        actualFolder = "a:/f1/f1/f1/f1";
        expResult = "..";
        result = Utility.getRelativePath(newAbsolPath, actualFolder);
        assertEquals(expResult, result);

        newAbsolPath = "a:/";
        actualFolder = "a:/f1/f1/f1/f1";
        expResult = "../../../..";
        result = Utility.getRelativePath(newAbsolPath, actualFolder);
        assertEquals(expResult, result);

        newAbsolPath = "a:/f1/f1/f1/f1/f1";
        actualFolder = "a:/";
        expResult = "f1/f1/f1/f1/f1";
        result = Utility.getRelativePath(newAbsolPath, actualFolder);
        assertEquals(expResult, result);

    }

    /**
     * Test of getLastFolder method, of class Utility.
     */
    @Test
    public void testGetLastFolder() {
        String path = "a:/abc/def";
        String expResult = "def";
        String result = Utility.getLastFolder(path);
        assertEquals(expResult, result);

        path = "a:/abc/def/";
        expResult = "def";
        result = Utility.getLastFolder(path);
        assertEquals(expResult, result);

        path = "abc/def";
        expResult = "def";
        result = Utility.getLastFolder(path);
        assertEquals(expResult, result);

        path = "abc/def/";
        expResult = "def";
        result = Utility.getLastFolder(path);
        assertEquals(expResult, result);

        path = "";
        expResult = "";
        result = Utility.getLastFolder(path);
        assertEquals(expResult, result);


    }

    /**
     * Test of removeLastFolder method, of class Utility.
     */
    @Test
    public void testRemoveLastFolder() {
        String path = "a:/abc/def";
        String expResult = "a:/abc";
        String result = Utility.removeLastFolder(path);
        assertEquals(expResult, result);

        path = "a:/abc/def/";
        expResult = "a:/abc";
        result = Utility.removeLastFolder(path);
        assertEquals(expResult, result);

        path = "abc/def";
        expResult = "abc";
        result = Utility.removeLastFolder(path);
        assertEquals(expResult, result);

        path = "abc/def/";
        expResult = "abc";
        result = Utility.removeLastFolder(path);
        assertEquals(expResult, result);

        path = "";
        expResult = "";
        result = Utility.removeLastFolder(path);
        assertEquals(expResult, result);
    }

    /**
     * Test of configLogger method, of class Utility.
     */
    @Test
    public void testConfigLogger() {
    }

    /**
     * Test of getLogger method, of class Utility.
     */
    @Test
    public void testGetLogger() {
    }

    /**
     * Test of getLenghtFromBytes method, of class Utility.
     */
    /**
     * Test of getTime method, of class Utility.
     */
    @Test
    public void testGetTime() {
//                January 1, 1970, 00:00:00
        Date d = new Date(0 - TimeZone.getDefault().getRawOffset());
        String expResult = "19700101T000000";
        String result = Utility.getTime(d);
        assertEquals(expResult, result);
    }

    /**
     * Test of threatResponse method, of class Utility.
     */
    @Test
    public void testThreatResponse() {
        Response badreq = new AbortResponse(Response.BADREQUEST);
        Response cont = new ConnectResponse(Response.CONTINUE);
        Response succ = new DisconnectResponse(Response.SUCCESS);
        Response creat = new PutResponse(Response.CREATED);
        assertFalse(Utility.threatResponse(badreq));
        assertTrue(Utility.threatResponse(cont));
        assertTrue(Utility.threatResponse(succ));
        assertTrue(Utility.threatResponse(creat));
        badreq.setFinal();
        cont.setFinal();
        succ.setFinal();
        creat.setFinal();
        assertFalse(Utility.threatResponse(badreq));
        assertTrue(Utility.threatResponse(cont));
        assertTrue(Utility.threatResponse(succ));
        assertTrue(Utility.threatResponse(creat));
    }

    /**
     * Test of buildPerm method, of class Utility.
     */
    @Test
    public void testBuildPerm() {
        byte[] expResult = {0x00, 0x05, '\"', 'R', 'W', 'D', '\"'};
        byte[] result = Utility.buildPerm(true, true, true, (byte) 0x00);
        assertArrayEquals(expResult, result);

        expResult = new byte[]{0x00, 0x02, '\"', '\"'};
        result = Utility.buildPerm(false, false, false, (byte) 0x00);
        assertArrayEquals(expResult, result);

        expResult = new byte[]{0x38, 0x05, '\"', 'R', 'W', 'D', '\"'};
        result = Utility.buildPerm(true, true, true, (byte) 0x38);
        assertArrayEquals(expResult, result);

        expResult = new byte[]{0x38, 0x04, '\"', 'R', 'D', '\"'};
        result = Utility.buildPerm(true, false, true, (byte) 0x38);
        assertArrayEquals(expResult, result);

        expResult = new byte[]{0x38, 0x04, '\"', 'W', 'D', '\"'};
        result = Utility.buildPerm(false, true, true, (byte) 0x38);
        assertArrayEquals(expResult, result);

        expResult = new byte[]{0x38, 0x04, '\"', 'R', 'W', '\"'};
        result = Utility.buildPerm(true, true, false, (byte) 0x38);
        assertArrayEquals(expResult, result);

        expResult = new byte[]{0x38, 0x03, '\"', 'R', '\"'};
        result = Utility.buildPerm(true, false, false, (byte) 0x38);
        assertArrayEquals(expResult, result);

        expResult = new byte[]{0x38, 0x03, '\"', 'W', '\"'};
        result = Utility.buildPerm(false, true, false, (byte) 0x38);
        assertArrayEquals(expResult, result);

        expResult = new byte[]{0x38, 0x03, '\"', 'D', '\"'};
        result = Utility.buildPerm(false, false, true, (byte) 0x38);
        assertArrayEquals(expResult, result);

    }

    /**
     * Test of bytesToGetResponse method, of class Utility.
     */
    @Test
    public void testBytesToGetResponse() throws Exception {
        String n = "smallFile";
        String c = "<smallFile><description>This is a small file</description><uses>Used to testTranslateGetResponse()</uses></smallFile>";
        c = c + c + c + c + c;
        int l = c.length();
        GetResponse req = new GetResponse(Response.CONTINUE);
        req.setFinal();

        Header header = new Header(Header.LENGTH);
        header.setValue(Utility.intToBytes(l, 4));
        req.addHeader(header);

        Header body;
        int maxPacketLenght = 500;
        InputStream fileInputStream = new ByteInputStream(c.getBytes(), l);
        int ava = fileInputStream.available();
        do {
            int size = (maxPacketLenght - 40) - (req.getPacketLength());
            byte[] b;
            if (ava < size + 3) {
                b = new byte[ava];
            } else {
                b = new byte[size - 3];
            }
            body = new Header(Header.BODY);
            fileInputStream.read(b);
            body.setValue(b);
            req.addHeader(body);
            assertArrayEquals(req.toBytes(), Utility.bytesToGetResponse(req.toBytes()).toBytes());
            req = new GetResponse(Response.CONTINUE);
            req.setFinal();
        } while ((ava = fileInputStream.available()) > 0);
        req = new GetResponse(Response.SUCCESS);
        req.setFinal();

    }

    /**
     * Test of humanReadableByteCount method, of class Utility.
     *                            SI     BINARY
     * 0:                        0 B        0 B
     * 27:                      27 B       27 B
     * 999:                    999 B      999 B
     * 1000:                  1.0 KB     1000 B
     * 1023:                  1.0 KB     1023 B
     * 1024:                  1.0 KB    1.0 KiB
     * 1728:                  1.7 KB    1.7 KiB
     * 110592:              110.6 KB  108.0 KiB
     * 7077888:               7.1 MB    6.8 MiB
     * 452984832:           453.0 MB  432.0 MiB
     * 28991029248:          29.0 GB   27.0 GiB
     * 1855425871872:         1.9 TB    1.7 TiB
     * 9223372036854775807:   9.2 EB    8.0 EiB   (Long.MAX_VALUE)
     */
    @Test
    public void testHumanReadableByteCount() {
        long bytes = 0L;
        boolean si = false;
        String expResult = "0 B";
        String result = Utility.humanReadableByteCount(bytes, si);
        assertEquals(expResult, result);
        result = Utility.humanReadableByteCount(bytes, !si);
        assertEquals(expResult, result);

        bytes = 1000L;
        si = false;
        expResult = "1000 B";
        result = Utility.humanReadableByteCount(bytes, si);
        assertEquals(expResult, result);

        expResult = "1,0 kB";
        result = Utility.humanReadableByteCount(bytes, !si);
        assertEquals(expResult, result);

        bytes = Long.MAX_VALUE;
        si = false;
        expResult = "8,0 EiB";
        result = Utility.humanReadableByteCount(bytes, si);
        assertEquals(expResult, result);

        expResult = "9,2 EB";
        result = Utility.humanReadableByteCount(bytes, !si);
        assertEquals(expResult, result);
    }

    /**
     * Test of dateFormat method, of class Utility.
     */
    @Test
    public void testDateFormat() {
        //no need of testing.
    }

    /**
     * Test of listingFormat method, of class Utility.
     */
    @Test
    public void testListingFormat() {
        //no need of testing
    }

    /**
     * Test of createSimbolicFolderTree method, of class Utility.
     */
    @Test
    public void testCreateSimbolicFolderTree() {
        String absolutePath = "a:/a/really/big/path/to/auto/create";
        OBEXFolder result = Utility.createSimbolicFolderTree(absolutePath);
        assertEquals(absolutePath, result.getPath());
    }

    /**
     * Test of getTime method, of class Utility.
     */
    @Test
    public void testGetTime_Date() {

    }

    /**
     * Test of buildPerm method, of class Utility.
     */
    @Test
    public void testBuildPerm_4args() {

    }

    /**
     * Test of bytesToName method, of class Utility.
     */
    @Test
    public void testBytesToName() {

    }

    /**
     * Test of buildPerm method, of class Utility.
     */
    @Test
    public void testBuildPerm_String_byte() {

    }

    /**
     * Test of getTime method, of class Utility.
     */
    @Test
    public void testGetTime_String() {

    }
}
