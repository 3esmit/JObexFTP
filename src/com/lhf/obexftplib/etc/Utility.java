/**
 *    Created on Oct 21, 2010
 *    This file is part of JObexFTP 2.0, and it contains parts of OBEX4J.
 *
 *    JObexFTP is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    JObexFTP is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with JObexFTP.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.lhf.obexftplib.etc;

import com.lhf.obexftplib.fs.OBEXFolder;
import com.lhf.obexftplib.io.obexop.GetResponse;
import com.lhf.obexftplib.io.obexop.Header;
import com.lhf.obexftplib.io.obexop.Response;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public final class Utility {

    private static final Logger OBEXLOGGER = Logger.getLogger("com.lhf.jobexftp");

    public static void configLogger(final Level loggingLevel) {
        Handler handler = null;
        handler = new ConsoleHandler();
        handler.setLevel(loggingLevel);
        OBEXLOGGER.setLevel(loggingLevel);
        OBEXLOGGER.addHandler(handler);
        OBEXLOGGER.setUseParentHandlers(false);
    }

    public static Logger getLogger() {
        return OBEXLOGGER;
    }

    public static String readStandardInput() {
        StringBuilder buf = new StringBuilder();
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
            }
            try {
                int c = System.in.read();
                if (c != 13) {
                    if (c == 10) {
                        break;
                    } else {
                        buf.append((char) c);
                    }
                }
                if (c == -1) {
                    return null;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return buf.toString().trim();
    }

    /**
     *  This is a hack to ports like /dev/ttyACM0 work in RxTx.
     * @param connPortPath the port path to add;
     * @since 1.0
     */
    public static CommPortIdentifier addPortToRxTx(final String connPortPath) throws NoSuchPortException {
        if (connPortPath.indexOf("ttyS") < 0 && connPortPath.indexOf("COM") < 0) {
            System.setProperty("gnu.io.rxtx.SerialPorts", connPortPath);
        }
        return CommPortIdentifier.getPortIdentifier(connPortPath);
    }

    /**
     * Checks for the combination looking from end to begining, in the whole array
     * @param b
     * @return true if found, false otherwise
     */
    public static boolean arrayContainsOK(final byte[] b) {
        for (int i = b.length - 1; i >= 3; i--) {
            if (b[i] == 13
                    && b[i - 1] == 75
                    && b[i - 2] == 79
                    && b[i - 3] == 10) {
                return true;
            }
        }
        return false;
    }

    public static boolean arrayContains(final byte[] what, final byte[] b) {
        boolean r = false;
        for (int i = b.length - 1; i > what.length; i--) {
            int j = what.length;
            while (j > -1) {
                if (b[i] != what[j]) {
                    r = false;
                    continue;
                } else {
                    r = true;
                }
            }
            if (r) {
                return r;
            }
        }
        return r;
    }

    public static String[] split(String input) {
        if (!input.contains("\"")) {
            return input.split(" ");
        }
        ArrayList<String> l = new ArrayList<String>();
        boolean isInQuotes = false;
        String stack = "";
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '\"') {
                if (isInQuotes) {
                    if (stack.length() > 0) {
                        l.add(stack);
                        stack = "";
                    }
                    isInQuotes = false;
                } else {
                    isInQuotes = true;
                }
            } else if (input.charAt(i) == ' ') {
                if (isInQuotes) {
                    stack += " ";
                } else {
                    if (stack.length() > 0) {
                        l.add(stack);
                        stack = "";
                    }
                }
            } else {
                stack += input.charAt(i);
            }
        }
        if (stack.length() > 0) {
            l.add(stack);
            stack = "";
        }
        return l.toArray(new String[l.size()]);
    }

    /**
     * convert integer to byte array
     *
     * @param integer the input integer to convert
     * @param bLength the length of result bytes array
     * @return byte array representing for the integer (Networking order)
     */
    public static byte[] intToBytes(final int integer, final int bLength) {
        if (integer > Integer.MAX_VALUE || integer < Integer.MIN_VALUE) {
            return null;
        }
        byte[] bytes = new byte[bLength];
        for (int i = 0; i < bLength; i++) {
            bytes[i] = new Integer(integer >> (bLength - 1 - i) * 8).byteValue();
        }
        return bytes;
    }

    /**
     * convert given byte array to integer
     *
     * @param bytes
     * @return integer represented by the given byte array
     */
    public static int bytesToInt(final byte[] bytes) {
        int result = 0;

        for (int i = 0; i < bytes.length; i++) {
            int temp = (int) bytes[i];
            if (temp < 0) {
                temp = 0x100 + temp;
            }
            result += temp << (8 * (bytes.length - 1 - i));
        }

        return result;
    }

    public static long bytesToLong(final byte[] bytes) {
        long result = 0;

        for (int i = 0; i < bytes.length; i++) {
            int temp = (int) bytes[i];
            if (temp < 0) {
                temp = 0x100 + temp;
            }
            result += temp << (8 * (bytes.length - 1 - i));
        }

        return result;
    }

    /**
     * get sub byte array from the given byte array
     *
     * @param data given byte array
     * @param offset start point of the sub array
     * @param length length of the sub array
     * @return sub byte array begins from data[offset]
     */
    public static byte[] getBytes(final byte[] data, int offset, final int length) {
        if (offset + length > data.length) {
            return null;
        }

        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = data[offset++];
        }
        return result;
    }

    /**
     * add a bunch of bytes to the byte array with offset
     *
     * @param dest bytes array which will contain the given bytes after the call
     * @param data a bunch of bytes to be added to the array
     * @param offset from where the data to be add to the dest
     * @param length length of the bytes to be add
     */
    public static void setBytes(final byte[] dest, final byte[] data, final int offset,
            int length) {
        if (offset + length > dest.length) {
            return;
        }
        for (int i = 0; i < length; i++) {
            dest[offset + i] = data[i];
        }
    }

    /**
     * convert the given byte to Hex String
     *
     * @param inbyte
     * @return HexString
     */
    public static String byteToHexString(final byte inbyte) {
        String result = "0x";
        if (inbyte <= Byte.MAX_VALUE && inbyte >= 0) {
            if (inbyte < 16) {
                result += "0";
            }
            result += Integer.toHexString((int) inbyte);
        } else {
            result += Integer.toHexString(0x100 + inbyte);
        }
        return result;
    }

    /**
     * convert the list of ByteArray to a single byte array
     *
     * @param list List whose elements are instances of ByteArray class
     * @return single byte array result in the concatenation of all the
     * elements of the given list
     */
    static byte[] byteArrayListToBytes(final ArrayList list) {
        if (list == null) {
            return null;
        }
        Iterator iter = list.iterator();
        if (iter == null) {
            return null;
        }
        int length = 0;
        while (iter.hasNext()) {
            Object element = iter.next();
            if (!ByteArray.class.isInstance(element)) {
                return null;
            }
            ByteArray ba = (ByteArray) iter.next();
            length += ba.length();
        }
        if (length == 0) {
            return null;
        }
        byte[] bytes = new byte[length];
        iter = list.iterator();
        int offset = 0;
        while (iter.hasNext()) {
            ByteArray ba = (ByteArray) iter.next();
            setBytes(bytes, ba.getBytes(), offset, ba.length());
            offset += ba.length();
        }
        return bytes;
    }

    /**
     * dump the content of the rawdata, this method is used by finest logging
     *
     * @param bytes byte array to be dump
     * @return String contains rawdata information
     */
    public static String dumpBytes(final byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            result += byteToHexString(bytes[i]).replace("0x", "");
            if (i == bytes.length - 1) {
                break;
            }
            result += ", ";
            if (i % 5 == 4) {
                result += "\n";
            }
        }
        return result;
    }

    /**
     * compare two byte arrays
     *
     * @param a byte array a
     * @param b byte array b
     * @return true if the contents are the same, other wise return false
     */
    public static boolean compareBytes(final byte[] a, final byte[] b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.length != b.length) {
            return false;
        }

        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static GetResponse bytesToGetResponse(final byte[] incomingData) {
        GetResponse response = new GetResponse(incomingData[0]);
        for (int i = 3; i < incomingData.length; i++) {
            Header header = new Header(incomingData[i]);
            //switch type of headers;
            int hLen;
            switch (header.getId()) {
                case Header.CONNECTION_ID:
                case Header.LENGTH:
                    header.setValue(getBytes(incomingData, ++i, 4));
                    i += 3;
                    break;
                case Header.TYPE:
                case Header.NAME:
                case Header.END_OF_BODY:
                case Header.BODY:
                    hLen = bytesToInt(getBytes(incomingData, ++i, 2)) - 3;
                    i++;
                    header.setValue(getBytes(incomingData, ++i, hLen));
                    i += hLen - 1;
                    break;
                case Header.DESCRIPTION:
                case Header.HTTP:
                    continue; //TODO: Threat this headers!;
            }
            response.addHeader(header);
        }
        return response;
    }

    /**
     * Builds a byte array for names in obex format
     * @param name
     * @return
     * @see Utility#bytesToName(byte[])
     */
    public static byte[] nameToBytes(final String name) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < name.length(); i++) {
            bos.write(0);
            bos.write((int) name.charAt(i));
        }
        byte[] b = bos.toByteArray();
        bos.reset();
        bos = null;
        return b;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static byte[] prepareMoveByteArray(String oldPath, String newPath) throws IOException {
        byte[] newPathB = Utility.nameToBytes(newPath), oldPathB = Utility.nameToBytes(oldPath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(oldPathB.length + newPathB.length + 10);
        baos.write(new byte[]{0x34, 0x04, 0x6D, 0x6F, 0x76, 0x65});
        baos.write(0x35);
        baos.write(oldPathB.length);
        baos.write(oldPathB);
        baos.write(0x36);
        baos.write(newPathB.length);
        baos.write(newPathB);
        byte[] b = baos.toByteArray();
        baos.close();
        return b;
    }

    /**
     * Replaces all slashes '\' for backslashes '/', removes the last backslash and remove the drive letter 'a:'
     * @param path the path to be prepared
     * @return the prepared path
     * @see Utility#removeLastSlash(java.lang.String)
     */
    public static String preparePath(String path) {
        path = path.replace('\\', '/'); //
        if (path.startsWith("a:/")) {
            path = path.replaceFirst("a:", "");
        }
        return removeLastSlash(path);
    }

    /**
     * Removes the last slash ('/') from any string.
     * @param path
     * @return
     */
    public static String removeLastSlash(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Compares the two absolute pathes and build an realtive path to get in the
     * @param absolutePath The absolute path to some file or folder
     * @param actualFolder The absolute path of the origin folder
     * @return The realtive path from actualFolder to absolutePath
     */
    public static String getRelativePath(final String absolutePath, final String actualFolder) {
        StringBuilder newPath = new StringBuilder();
        boolean notEq = false;
        String newFolderList[] = absolutePath.split("/");
        String actFolderList[] = actualFolder.split("/");
        int j = actFolderList.length;
        for (int i = 0; i < actFolderList.length; i++) {
            String actFolder = actFolderList[i];
            String newFolder = i < newFolderList.length ? newFolderList[i] : null;
            if (notEq || !actFolder.equalsIgnoreCase(newFolder)) {
                newPath.append("../");
                if (!notEq) {
                    notEq = true;
                    j = i;
                }
            }
        }
        for (; j < newFolderList.length; j++) {
            newPath.append(newFolderList[j]).append("/");
        }
        return removeLastSlash(newPath.toString());
    }

    /**
     * gets the last folder form a path.
     * @param path an absolute or relative path
     * @return the last folder
     */
    public static String getLastFolder(String path) {
        path = removeLastSlash(path);
        path = path.substring(path.lastIndexOf("/") + 1, path.length());
        return path;
    }

    /**
     * gets the last folder form a path.
     * @param path an absolute or relative path
     * @return the last folder
     */
    public static String removeLastFolder(String path) {
        path = removeLastSlash(path);
        int n = path.lastIndexOf("/");
        if (n > 0) {
            path = path.substring(0, n);
        }
        return path;
    }

    /**
     * This function is used when the Path is known, but there is no OBEXFolder referenciating it.
     * So it makes easier to user OBEXFolder to movein or create folders.
     * @param absolutePath an absolutpath, starting with a:/ or /
     * @return the last level OBEXFolder of the path specified.
     */
    public static OBEXFolder createSimbolicFolderTree(String absolutePath) {
        if (!(absolutePath.startsWith("/") || absolutePath.startsWith("a:"))) {
            absolutePath = "a:/" + absolutePath;
        }
        OBEXFolder folder = OBEXFolder.ROOT_FOLDER;
        String pathList[] = absolutePath.split("/");
        for (int i = 0; i < pathList.length; i++) {
            if (!pathList[i].startsWith("a:")) {
                folder = folder.addFolder(pathList[i]);
            }
        }
        return folder;
    }
    /**
     * DateFormat for getTime
     * @see Utility#getTime(java.util.Date)
     */
    private static final DateFormat OBEX_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

    /**
     * Builds a string in the SimpleDateFormat of yyyyMMdd'T'HHmmss
     * @param time the time
     * @return String representation of the SimpleDateFormat of yyyyMMdd'T'HHmmss in the specified time
     */
    public static String getTime(final Date time) {
        return OBEX_DATE_FORMAT.format(time);
    }

    /**
     * Helper to check response values
     * @param res
     * @return true if is Response.CONTINUE Response.SUCCESS Response.CREATED, false otherwise
     */
    public static boolean threatResponse(final Response res) {
        boolean b = false;
        switch (res.getType() & 0x7F) {
            case Response.CONTINUE:
                b = true;
                break;
            case Response.SUCCESS:
                b = true;
                break;
            case Response.BADREQUEST:
                b = false;
                break;
            case Response.CREATED:
                b = true;
                break;
            default:
                b = false;
        }

        return b;
    }

    /**
     * Creates byte array to set user, group and others permissions.
     * @param read if read granted
     * @param write if write granted
     * @param delete if delete granted
     * @param type the type to user, group or others.
     * @return the array
     */
    public static byte[] buildPerm(final boolean read, final boolean write, final boolean delete, final byte type) {
        byte[] prem;
        int i = 4 + (read ? 1 : 0) + (write ? 1 : 0) + (delete ? 1 : 0);
        int j = i;
        prem = new byte[i];

        prem[--i] = '\"';
        if (delete) {
            prem[--i] = 'D';
        }
        if (write) {
            prem[--i] = 'W';
        }
        if (read) {
            prem[--i] = 'R';
        }
        prem[--i] = '\"';
        prem[--i] = (byte) (j - 2);
        prem[--i] = type;
        return prem;
    }

    /**
     * Builds the name from bytes used by OBEX
     * @param name the name in obex format
     * @return the name in human readable
     * @see Utility#nameToBytes(java.lang.String)
     */
    public static String bytesToName(final byte[] name) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length; i++) {
            if (name[i] != 0) {
                builder.append((char) name[i]);
            }
        }
        return builder.toString();
    }

    /**
     * Creates byte array to set user, group and others permissions.
     * @param userPerm the string containing the permited operations.
     * @param type the type to user, group or others.
     * @return the array
     */
    public static byte[] buildPerm(final String userPerm, final String groupPerm) {
        byte[] prem;
        int i = 6 + userPerm.length() + groupPerm.length();
        int j = i;
        prem = new byte[i];
        prem[--i] = '\"';
        for (int k = groupPerm.length() - 1; k > -1; k--) {
            prem[--i] = (byte) groupPerm.charAt(k);
        }
        prem[--i] = '\"';
        prem[--i] = '\"';
        for (int k = userPerm.length() - 1; k > -1; k--) {
            prem[--i] = (byte) userPerm.charAt(k);
        }
        prem[--i] = '\"';
        prem[--i] = (byte) (j - 2);
        prem[--i] = 0x38;
        return prem;
    }

    public static String bytesToPerm(byte[] perm) {
        StringBuilder s = new StringBuilder();
        for (int i = 3; i < perm.length; i++) {
            byte b = perm[i];
            if (b != '\"') {
                s.append((char) b);
            }
        }
        return s.toString();
    }
    private static final DateFormat format = SimpleDateFormat.getInstance();

    public static String dateFormat(final Date date) {
        return format.format(date);
    }

    public static StringBuilder listingFormat(StringBuilder builder, String filename, String size, Date date) {
        int l = builder.length();
        builder.append(filename).append("                    ").setLength(l + 20);
        builder.append(size).append("           ").setLength(l + 30);
        if (date != null) {
            builder.append(Utility.dateFormat(date));
        }
        builder.append('\n');

        return builder;
    }

    public static StringBuilder listingFormat(StringBuilder builder, String filename, String size, Date date, String perm1, String perm2) {
        int l = builder.length();
        builder.append(filename).append("                    ").setLength(l = l + 20);
        builder.append(size).append("           ").setLength(l = l + 10);
        builder.append(perm1).append("    ").setLength(l = l + 4);
        builder.append(perm2).append("    ").setLength(l = l + 4);
        if (date != null) {
            builder.append(Utility.dateFormat(date));
        }
//        builder.append("               ").setLength(l = l + 15);

        builder.append('\n');

        return builder;
    }

    /**
     * Gets a long representation of a yyyyMMdd'T'HHmmss time representated String
     * @param value
     * @return
     * @see Utility#getTime(java.util.Date)
     */
    public static Date getTime(final String value) {
        Calendar c = GregorianCalendar.getInstance();
        int year = Integer.parseInt(value.substring(0, 4));
        int month = Integer.parseInt(value.substring(4, 6));
        int date = Integer.parseInt(value.substring(6, 8));
        int hrs = Integer.parseInt(value.substring(9, 11));
        int min = Integer.parseInt(value.substring(11, 13));
        int sec = Integer.parseInt(value.substring(13, 15));
        c.set(year, month - 1, date, hrs, min, sec);
        return c.getTime();
    }
}

/**
 * Object wrapper for byte array
 *
 * @author Joey Shen
 */
class ByteArray {

    private byte[] bytes = null;

    public ByteArray(final byte[] inbytes) {
        bytes = inbytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int length() {
        if (bytes == null) {
            return 0;
        } else {
            return bytes.length;
        }
    }
}
