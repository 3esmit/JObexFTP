/**
 *    This file is part of JObexFTP 2.0, and contains parts of OBEX4J.
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
package com.lhf.obexftplib.io.obexop;

import com.lhf.obexftplib.etc.Utility;
import java.util.Iterator;

/**
 *
 * @author Ricardo Guilherme Schmidt
 */
public class SetPathRequest extends Request {

    private static final int HEADEROFFSET = 5;
    protected byte flags = (byte) 0;
    protected int MaxPacketLength = Packet.MAXPACKETLENGTH;

    /**
     * default constructor
     */
    public SetPathRequest() {
        opcode = Request.SETPATH ;
        headeroffset = HEADEROFFSET;
        PacketLength = 5;
    }

    /**
     * creat instance of ConnectRequest from the rawdata
     *
     * @param data
     */
    public SetPathRequest(byte[] data) {
        opcode = Request.SETPATH ;
        headeroffset = HEADEROFFSET;
        rawdata = data;
        parseRawData(rawdata);
    }

    /**
     * create instance of ConnectRequest from the HeaderSet
     *
     * @param inHeaders
     */
    public SetPathRequest(HeaderSet inHeaders) {
        opcode = Request.SETPATH ;
        headeroffset = HEADEROFFSET;
        PacketLength = 5;
        if (inHeaders == null) {
            return;
        }
        Iterator headerIter = inHeaders.iterator();
        if (headerIter == null) {
            return;
        }
        while (headerIter.hasNext()) {
            Header header = (Header) headerIter.next();
            if (header != null) {
                headers.add(header);
                PacketLength += header.getLength();
            }
        }
    }

    /**
     * set flags field of the connect request
     *
     * @param inflags
     */
    public void setFlags(byte inflags) {
        flags = inflags;
    }

    /**
     * get flags field of the connect request
     *
     * @return flags field
     */
    public byte getFlags() {
        return flags;
    }

    /**
     * set MaxPacketLength field of the connect request
     *
     * @param length
     */
    public void setMaxPacketLenght(int length) {
        MaxPacketLength = length;
    }

    /**
     * get MaxPacketLength from the connect request
     *
     * @return MaxPacketLength contained in the connect request
     */
    public int getMaxPacketLength() {
        return MaxPacketLength;
    }

    protected void fillPacketFields() {
        if (rawdata == null) {
            return;
        }

        rawdata[0] = opcode;
        byte[] tmpBytes = Utility.intToBytes(PacketLength, 2);
        Utility.setBytes(rawdata, tmpBytes, 1, 2);
        rawdata[3] = flags;
        tmpBytes = Utility.intToBytes(MaxPacketLength, 2);
        Utility.setBytes(rawdata, tmpBytes, 5, 2);
    }

    protected void parsePacketFields(byte[] data) {
        opcode = data[0];
        flags = data[3];

        byte[] tmpArray = Utility.getBytes(data, 5, 2);
        MaxPacketLength = Utility.bytesToInt(tmpArray);
    }

    public String packetFieldsToString() {
        String result = "ConnectRequest:\n";
        result += "opcode: " + Utility.byteToHexString(opcode) + "\n";
        result += "Packet Length: " + PacketLength + "\n";
        result += "Flags: " + Utility.byteToHexString(flags) + "\n";
        result += "Max OBEX Packet Length: " + MaxPacketLength + "\n";
        return result;
    }
}
