/**
 * Created on Nov 1, 2004
 *
 *    This file is part of OBEX4J, used by JObexFTP 2.0.
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
 * sub class of Request representing the Disconnect Request
 * 
 * @author Joey Shen (joey.shen@sun.com)
 */
public class DisconnectRequest extends Request {
    private static final int HEADEROFFSET = 3;
    
    /**
     * default constructor of Disconnect Request
     */
    public DisconnectRequest() {
        opcode = Request.DISCONNECT;
        headeroffset = HEADEROFFSET;
        PacketLength = 3;
    }
    
    /**
     * creat instance of ConnectRequest from the rawdata
     * 
     * @param data
     */
    public DisconnectRequest(byte[] data) {
        opcode = Request.DISCONNECT;
        headeroffset = HEADEROFFSET;
        rawdata = data;
        parseRawData(rawdata);
    }
    
    /**
     * create instance of ConnectRequest from the HeaderSet
     * 
     * @param inHeaders
     */
    public DisconnectRequest(HeaderSet inHeaders) {
        opcode = Request.DISCONNECT;
        headeroffset = HEADEROFFSET;
        PacketLength = 3;
        if (inHeaders == null) {
            return;   
        }
        Iterator headerIter = inHeaders.iterator();
        if (headerIter == null) {
            return;   
        }
        while (headerIter.hasNext()) {
            Header header = (Header)headerIter.next();
            if (header != null) {
                headers.add(header);
                PacketLength += header.getLength();
            }
        }
    }

    protected void fillPacketFields() {
        if (rawdata == null) {
            return;
        }
        rawdata[0] = opcode;
        byte[] tmpBytes = Utility.intToBytes(PacketLength, 2);
        Utility.setBytes(rawdata, tmpBytes, 1, 2);

    }

    protected void parsePacketFields(byte[] data) {
        opcode = data[0];
    }
    
    protected String packetFieldsToString() {
        String result = "DisconnectRequest:\n"; 
        result += "opcode: " + Utility.byteToHexString(opcode) + "\n";
        result += "Packet Length: " + PacketLength + "\n";
        return result;
    }
}