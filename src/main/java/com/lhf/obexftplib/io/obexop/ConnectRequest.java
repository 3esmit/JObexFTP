/**
 * Created on Oct 22, 2004
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
 * Sub class of Request, specific for the connect request
 * 
 * @author Joey Shen (joey.shen@sun.com)
 */
public class ConnectRequest extends Request {
    /**
     * default OBEX Version Number 1.3
     */
    public static final byte DEFAULT_VERSION_NUMBER = 0x13; 
    
    private static final int HEADEROFFSET = 7;
    
    protected byte VersionNumber;
    protected byte flags = (byte)0;
    protected int MaxPacketLength = Packet.MAXPACKETLENGTH;
    
    /**
     * default constructor
     */
    public ConnectRequest() {
        opcode = Request.CONNECT;
        headeroffset = HEADEROFFSET;
        PacketLength = 7;
        VersionNumber = DEFAULT_VERSION_NUMBER;
    }

    /**
     * creat instance of ConnectRequest from the rawdata
     * 
     * @param data
     */
    public ConnectRequest(byte[] data) {
        opcode = Request.CONNECT;
        headeroffset = HEADEROFFSET;
        rawdata = data;
        parseRawData(rawdata);
    }
    
    /**
     * create instance of ConnectRequest from the HeaderSet
     * 
     * @param inHeaders
     */
    public ConnectRequest(HeaderSet inHeaders) {
        opcode = Request.CONNECT;
        headeroffset = HEADEROFFSET;
        VersionNumber = DEFAULT_VERSION_NUMBER;
        PacketLength = 7;
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
    
    /**
     * set the version number field of the connect request
     * 
     * @param number
     */
    public void setVersionNumber(byte number) {
        VersionNumber = number;
    }
    
    /**
     * get OBEX version number information from the connect request
     * 
     * @return OBEX version number 
     */
    public byte getVersionNumber() {
        return VersionNumber;
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
        rawdata[3] = VersionNumber;
        rawdata[4] = flags;
        tmpBytes = Utility.intToBytes(MaxPacketLength, 2);
        Utility.setBytes(rawdata, tmpBytes, 5, 2);
    }
    
    protected void parsePacketFields(byte[] data) {
        opcode = data[0];
        VersionNumber = data[3];
        flags = data[4];

        byte[] tmpArray = Utility.getBytes(data, 5, 2);
        MaxPacketLength = Utility.bytesToInt(tmpArray);
    }
    

    public String packetFieldsToString() {
        String result = "ConnectRequest:\n"; 
        result += "opcode: " + Utility.byteToHexString(opcode) + "\n";
        result += "Packet Length: " + PacketLength + "\n";
        result += "OBEX Version Number: " + Utility.byteToHexString(
                VersionNumber) + "\n";
        result += "Flags: " + Utility.byteToHexString(flags) + "\n";
        result += "Max OBEX Packet Length: " + MaxPacketLength + "\n";
        return result;
    }
}