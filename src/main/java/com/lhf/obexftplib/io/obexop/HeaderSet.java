/**
 * Created on Sep 19, 2004
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
import java.util.ArrayList;
import java.util.Iterator;
/**
 * Header list contains all headers a OBEX packet contains
 * 
 * @author Joey Shen
 */
public final class HeaderSet<T> extends ArrayList<Header> {
    private byte[] rawdata;
    
    public HeaderSet() {
        super();
    }
    /**
     * construct HeaderSet from rawdata
     * 
     * @param bytes
     */
    public HeaderSet(final byte[] bytes) {
        rawdata = bytes;
        parseHeaders(rawdata);
    }
    
    /**
     * set all the headers contained in the HeaderSet
     */
    protected void parseHeaders(final byte[] data) {
        int bytesleft = data.length;
        int offset = 0;
        int headerlength = 0;
        
        while (bytesleft != 0) {
            byte tmpByte = data[offset];
            if (Header.hasLengthField(tmpByte)) {
                headerlength = Utility.bytesToInt(
                        Utility.getBytes(data, offset + 1, 2));
            } else {
                if ((tmpByte & 0xC0) == 0xC0) {
                    headerlength = 2;
                } else if ((tmpByte & 0x80) == 0x80){
                    headerlength = 5;
                }
            }
            Header tmpHeader = new Header(
                    Utility.getBytes(data, offset, headerlength));
            this.add(tmpHeader);
            offset += headerlength;
            bytesleft -= headerlength;
        }
    }
    
    /**
     * get the header value of certain header
     * 
     * @param id the header id which are interested
     * @return header value related to the header whose id was input
     */
    public byte[] getHeaderValue(final byte id) {
        Header header = getHeader(id);
        if (header == null) {
            return null;
        } else {
            return header.getValue();   
        }
    }
    
    /**
     * get header from the HeaderSet according to the input id
     * 
     * @param id id of the header wanted
     * @return the header wanted
     */
    public Header getHeader(final byte id) {
        Iterator iter = this.iterator();
        if (iter == null) {
            return null;   
        }
        while (iter.hasNext()) {
            Header tmpHeader = (Header)iter.next();
            if (tmpHeader.getId() == id) {
                return tmpHeader;   
            }
        }
        return null;
    }
    
    /**
     * get the total length of all the headers contained in the HeaderSet
     * 
     * @return total length of the headers contained in the HeaderSet
     */
    public int getTotalLength() {
        int length = 0;
        Iterator iter = this.iterator();
        while (iter != null && iter.hasNext()) {
            Header header = (Header)iter.next();
            length += header.getLength();
        }
        
        return length;
    }
    
    @Override
    public String toString() {
        String result = "";
        Iterator iter = this.iterator();
        
        while (iter != null && iter.hasNext()) {
            result += iter.next().toString();
            result += "\n";
        }
        
        return result;
    }
}