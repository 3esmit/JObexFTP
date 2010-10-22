/**
 * Created on 2004-11-9
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
 * subclass of Request representing Put Request
 * 
 * @author Joey Shen (joey.shen@sun.com)
 */
public class PutRequest extends Request {
	private static final int HEADEROFFSET = 3;
	
	/**
	 * default contructor
	 */
	public PutRequest() {
		opcode = Request.PUT;
		headeroffset = HEADEROFFSET;
		PacketLength = 3;
	}
	
	/**
	 * construct PutRequest from rawdata
	 * 
	 * @param data rawdata
	 */
	public PutRequest(byte[] data) {
		opcode = Request.PUT;
		headeroffset = HEADEROFFSET;
		rawdata = data;
		parseRawData(data);
	}
	
	/**
	 * construct Put Request according to the given HeaderSet
	 * 
	 * @param inHeaders given HeaderSet
	 */
	public PutRequest(HeaderSet inHeaders) {
		opcode = Request.PUT;
		headeroffset = HEADEROFFSET;
		PacketLength = 3;
		if (inHeaders == null) {
			return;
		}
		Iterator iter = inHeaders.iterator();
		while (iter != null && iter.hasNext()) {
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
    	rawdata[0] = opcode;
    	byte[] tmpBytes = Utility.intToBytes(PacketLength, 2);
    	Utility.setBytes(rawdata, tmpBytes, 1, 2);
	}

	/* (non-Javadoc)
	 * @see org.UniSync.OBEX.Packet#parsePacketFields(byte[])
	 */
	protected void parsePacketFields(byte[] data) {
		opcode = data[0];
	}

	/* (non-Javadoc)
	 * @see org.UniSync.OBEX.Packet#packetFieldsToString()
	 */
	protected String packetFieldsToString() {
		String result = "PutRequest:\n"; 
        result += "opcode: " + Utility.byteToHexString(opcode) + "\n";
        result += "Packet Length: " + PacketLength + "\n";
        return result;
	}
}