/**
 * Created on 2004-11-10
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
 * @author Joey Shen (joey.shen@sun.com)
 */
public class GetResponse extends Response {
    private static final int HEADEROFFSET = 3;

    /**
     * construct Get Response with the given response code
     * 
     * @param atype given response code
     */
    public GetResponse(byte atype) {
        respcode = atype;
        headeroffset = HEADEROFFSET;
        PacketLength = 3;
    }
    
    /**
     * construct Get Response according to the rawdata
     * 
     * @param data rawdata representing the Put Response
     */
    public GetResponse(byte[] data) {
        rawdata = data;
        headeroffset = HEADEROFFSET;
        parseRawData(data);
    }
    
    /**
     * construct Get Response with the given HeaderSet
     * 
     * @param atype response code
     * @param inHeaders given HeaderSet
     */
    public GetResponse(byte atype, HeaderSet inHeaders) {
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
    
    /* (non-Javadoc)
     * @see org.UniSync.OBEX.Packet#fillPacketFields()
     */
    protected void fillPacketFields() {
        if (rawdata == null) {
            return;
        }
        rawdata[0] = respcode;
        byte[] tmpBytes = Utility.intToBytes(PacketLength, 2);
        Utility.setBytes(rawdata, tmpBytes, 1, 2);
    }

    /* (non-Javadoc)
     * @see org.UniSync.OBEX.Packet#parsePacketFields(byte[])
     */
    protected void parsePacketFields(byte[] data) {
        respcode = data[0];
    }

    /* (non-Javadoc)
     * @see org.UniSync.OBEX.Packet#packetFieldsToString()
     */
    protected String packetFieldsToString() {
        String result = "GetResponse:\n";
        result += "respcode: " + Utility.byteToHexString(respcode) + "\n";
        result += "Packet Length: " + PacketLength + "\n";
        return result;
    }
}