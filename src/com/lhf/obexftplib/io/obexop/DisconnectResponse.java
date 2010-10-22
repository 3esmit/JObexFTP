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
 * sub class of Response representing Disconnect Response
 * 
 * @author Joey Shen (joey.shen@sun.com)
 */
public class DisconnectResponse extends Response {
    private static final int HEADEROFFSET = 3;
    
    /**
     * constructor of ConnectResponse with different response code
     * 
     * @param atype represents the status code of the response
     */
    public DisconnectResponse(byte atype) {
        respcode = atype;
        headeroffset = HEADEROFFSET;
        PacketLength = 3;
    }

    /**
     * construct Connect Response from rawdata
     * 
     * @param data rawdata of the Connect Request
     */
    public DisconnectResponse(byte[] data) {
        rawdata = data;
        headeroffset = HEADEROFFSET;
        parseRawData(rawdata);
    }
    
    public DisconnectResponse(byte atype, HeaderSet inHeaders) {
        respcode = atype;
        headeroffset = HEADEROFFSET;
        PacketLength = 3;
        if (inHeaders == null) 
            return;
        Iterator iter = inHeaders.iterator();
        if (iter == null)
            return;
        while (iter.hasNext()) {
            Header header = (Header)iter.next();
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
        rawdata[0] = respcode;
        byte[] tmpBytes = Utility.intToBytes(PacketLength, 2);
        Utility.setBytes(rawdata, tmpBytes, 1, 2);
    }

    protected void parsePacketFields(byte[] data) {
        respcode = data[0];
    }
    
    protected String packetFieldsToString() {
        String result = "DisconnectRequest:\n"; 
        result += "respcode: " + Utility.byteToHexString(respcode) + "\n";
        result += "Packet Length: " + PacketLength + "\n";
        return result;
    }

}