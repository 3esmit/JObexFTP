/**
 * Created on Oct 27, 2004
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
 * sub class of Response representing the Connect Response
 * 
 * @author Joey Shen (joey.shen@sun.com)
 */
public class ConnectResponse extends Response {
    public static final byte DEFAULT_VERSION_NUMBER = 0x13; 

    private static final int HEADEROFFSET = 7;
    
    protected byte VersionNumber;
    protected byte flags = (byte)0;
    protected int MaxPacketLength;
    
    /**
     * constructor of ConnectResponse with different response code
     * 
     * @param atype represents the status code of the response
     */
    public ConnectResponse(byte atype) {
        respcode = atype;
        headeroffset = HEADEROFFSET;
        VersionNumber = DEFAULT_VERSION_NUMBER;
        MaxPacketLength = Packet.MAXPACKETLENGTH;
        PacketLength = 7;
    }

    /**
     * construct Connect Response from rawdata
     * 
     * @param data rawdata of the Connect Request
     */
    public ConnectResponse(byte[] data) {
        rawdata = data;
        headeroffset = HEADEROFFSET;
        parseRawData(rawdata);
    }
    
    /**
     * construct Connect Response from incoming type and Headers
     * 
     * @param atype
     * @param inHeaders
     */
    public ConnectResponse(byte atype, HeaderSet inHeaders) {
        respcode = atype;
        headeroffset = HEADEROFFSET;
        VersionNumber = DEFAULT_VERSION_NUMBER;
        MaxPacketLength = Packet.MAXPACKETLENGTH;
        PacketLength = 7;
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
    
    /**
     * set the OBEX version number
     * 
     * @param number
     */
    public void setVersionNumber(byte number) {
        VersionNumber = number;
    }
    
    /**
     * get the OBEX version number
     * 
     * @return OBEX version number
     */
    public byte getVersionNumber() {
        return VersionNumber;
    }
    
    /**
     * set Max Packet Length
     * 
     * @param length
     */
    public void setMaxPacketLength(int length) {
        MaxPacketLength = length;
    }
    
    /**
     * get MaxPacketLength from the response
     * 
     * @return MaxPacketLength
     */
    public int getMaxPacketLength() {
        return MaxPacketLength;
    }
    
    protected void fillPacketFields() {
        if (rawdata == null) {
            return;
        }
        rawdata[0] = respcode;
        byte[] tmpBytes = Utility.intToBytes(PacketLength, 2);
        Utility.setBytes(rawdata, tmpBytes, 1, 2);
        rawdata[3] = VersionNumber;
        rawdata[4] = flags;
        tmpBytes = Utility.intToBytes(MaxPacketLength, 2);
        Utility.setBytes(rawdata, tmpBytes, 5, 2);
    }

    protected void parsePacketFields(byte[] data) {
        respcode = data[0];
        VersionNumber = data[3];
        flags = data[4];
        byte[] tmpArray = Utility.getBytes(data, 5, 2);
        MaxPacketLength = Utility.bytesToInt(tmpArray);
    }
    
    protected String packetFieldsToString() {
        String result = "ConnectResponse:\n"; 
        result += "respcode: " + Utility.byteToHexString(respcode) + "\n";
        result += "Packet Length: " + PacketLength + "\n";
        result += "OBEX Version Number: " + Utility.byteToHexString(
                VersionNumber) + "\n";
        result += "Flags: " + Utility.byteToHexString(flags) + "\n";
        result += "Max OBEX Packet Length: " + MaxPacketLength + "\n";
        return result;
    }
}