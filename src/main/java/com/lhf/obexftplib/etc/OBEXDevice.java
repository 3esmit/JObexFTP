/*
 * Last updated in 21/Out/2010
 *
 *    This file is part of JObexFTP 2.0.
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

/**
 *
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public enum OBEXDevice {

    TC65(
    new byte[]{'A', 'T', '^', 'S', 'Q', 'W', 'E', '=', '0', '\r'},
    new byte[]{'A', 'T', '^', 'S', 'Q', 'W', 'E', '=', '3', '\r'},
    new byte[]{(byte) 0x6B, (byte) 0x01, (byte) 0xCB, (byte) 0x31, (byte) 0x41, (byte) 0x06, (byte) 0x11, (byte) 0xD4, (byte) 0x9A, (byte) 0x77, (byte) 0x00, (byte) 0x50, (byte) 0xDA, (byte) 0x3F, (byte) 0x47, (byte) 0x1f}, new byte[]{'x', '-', 'o', 'b', 'e', 'x', '/', 'f', 'o', 'l', 'd', 'e', 'r', '-', 'l', 'i', 's', 't', 'i', 'n', 'g'},
    "a:"),
    DEFAULT(
    new byte[]{'A', 'T', '^', 'S', 'Q', 'W', 'E', '=', '0', '\r'},
    new byte[]{'A', 'T', '^', 'S', 'Q', 'W', 'E', '=', '3', '\r'},
    new byte[]{(byte) 0x6B, (byte) 0x01, (byte) 0xCB, (byte) 0x31, (byte) 0x41, (byte) 0x06, (byte) 0x11, (byte) 0xD4, (byte) 0x9A, (byte) 0x77, (byte) 0x00, (byte) 0x50, (byte) 0xDA, (byte) 0x3F, (byte) 0x47, (byte) 0x1f}, new byte[]{'x', '-', 'o', 'b', 'e', 'x', '/', 'f', 'o', 'l', 'd', 'e', 'r', '-', 'l', 'i', 's', 't', 'i', 'n', 'g'},
    "a:");
    private final byte[] obexCheck, obexOpen, fsUuid, lsName;
    private final String rootFolder;

    private OBEXDevice(byte[] obexCheck, byte[] obexOpen, byte[] fsUuid, byte[] lsNafme, String rootFolder) {
        this.obexCheck = obexCheck;
        this.obexOpen = obexOpen;
        this.fsUuid = fsUuid;
        this.lsName = lsNafme;
        this.rootFolder = rootFolder;
    }
    public static final byte[] CMD_CHECK = new byte[]{'A', 'T', '\r'};
    public static final byte[] ECHO_OFF = new byte[]{'A', 'T', 'E', '\r'};
    public static final byte[] ECHO_ON = new byte[]{'A', 'T', 'E', '1', '\r'};
    public static final byte[] TEST_DEVICE = new byte[]{'A', 'T', 'I', '\r'};
    public static final byte[] UNDO = new byte[]{'A', 'T', 'Z', '\r'};

    public byte[] getFlowControl(final byte flowControlMode) {
        return new byte[]{'A', 'T', '\\', 'Q', flowControlMode, '\r'};
    }

    /**
     * @return the obexCheck
     */
    public byte[] getObexCheck() {
        return obexCheck;
    }

    /**
     * @return the obexOpen
     */
    public byte[] getObexOpen() {
        return obexOpen;
    }

    /**
     * @return the fsUuid
     */
    public byte[] getFsUuid() {
        return fsUuid;
    }

    /**
     * @return the lsNafme
     */
    public byte[] getLsName() {
        return lsName;
    }

    /**
     * @return the rootFolder
     */
    public String getRootFolder() {
        return rootFolder;
    }
}
