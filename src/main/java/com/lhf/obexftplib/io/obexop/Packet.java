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
import java.util.Arrays;
import java.util.Iterator;

/**
 * abstract class representing all OBEX Packets
 * NOTE: consider Serialize later
 * 
 * @author Joey Shen
 */
public abstract class Packet {

    /**
     * default MaxPacketLength
     */
    public static final int MAXPACKETLENGTH = 64 * 1024 - 1;
    protected int PacketLength = 0;
    protected HeaderSet<Header> headers = new HeaderSet<Header>();
    protected byte[] rawdata = null;
    /* offset of the optional headers if there is any */
    protected int headeroffset;

    /**
     * set the length of the packet
     * 
     * @param length the length of the packet
     */
    public void setPacketLength(final int length) {
        PacketLength = length;
        if (rawdata != null) {
            byte[] pl = Utility.intToBytes(PacketLength, 2);
            rawdata[1] = pl[0];
            rawdata[2] = pl[1];
        }
    }

    /**
     * get the packet length
     * 
     * @return packet length
     */
    public int getPacketLength() {
        return PacketLength;
    }

    /**
     * add given header to the packet
     * 
     * @param header header to add
     */
    public void addHeader(final Header header) {
        headers.add(header);
        PacketLength += header.getLength();
    }

    /**
     * remove all headers from the packet
     */
    public void removeHeaders() {
        if (headers != null) {
            headers.clear();
        }
    }

    public Iterator<Header> getHeaders() {
        if (headers != null) {
            return headers.iterator();
        }
        return null;
    }

    /**
     * remove the header 
     * 
     * @param header to be removed
     */
    public void removeHeader(final Header header) {
        headers.remove(header);
        PacketLength -= header.getLength();
    }

    /**
     * get the value of the given Header Type
     * 
     * @param headerType
     * @return value of the given header
     */
    public byte[] getHeaderValue(final byte headerType) {
        return headers.getHeaderValue(headerType);
    }

    /**
     * get the Type of the packet
     * 
     * @return type of the packet
     */
    public abstract byte getType();

    /**
     * set Type of the packet
     * 
     * @param type type of the packet
     */
    public abstract void setType(byte type);

    /**
     * convert the request packet to rawdata
     * 
     * @return rawdata of the packet
     */
    public byte[] toBytes() {
        if (rawdata == null) {
            rawdata = new byte[PacketLength];

            fillPacketFields();
        }
        int index = headeroffset;
        byte[] tmpBytes;
        if (headers != null) {
            /* Be sure ConnectionID header always be the first header */
            Header connectionID = headers.getHeader(Header.CONNECTION_ID);
            if (connectionID != null) {
                tmpBytes = connectionID.toBytes();
                for (int i = 0; i < 5; i++) {
                    rawdata[index++] = tmpBytes[i];
                }
            }
            Iterator iter = headers.iterator();
            while (iter != null && iter.hasNext()) {
                tmpBytes = ((Header) iter.next()).toBytes();
                /* skip the ConnectionID header */
                if (tmpBytes[0] == Header.CONNECTION_ID) {
                    continue;
                }

                for (int i = 0; i < tmpBytes.length; i++) {
                    rawdata[index++] = tmpBytes[i];
                }
            }
        }
        return rawdata;
    }

    /**
     * fill Packet specific fields to the rawdata according to the specific type of packet
     */
    protected abstract void fillPacketFields();

    /**
     * parse the rawdata and set the relative fields and headers
     * 
     * @param data
     */
    protected void parseRawData(final byte[] data) {
        int offset = headeroffset;
        byte[] tmpArray = null;

        /*
         * get actual length instead of parsing from the rawdata
         */
        PacketLength = data.length;
        parsePacketFields(Utility.getBytes(data, 0, headeroffset));

        while (offset < PacketLength) {
            int headerLength = Header.getHeaderLength(data[offset]);
            if (headerLength == -1) {
                tmpArray = Utility.getBytes(data, offset + 1, 2);
                headerLength = Utility.bytesToInt(tmpArray);
            }

            Header header = new Header(Utility.getBytes(data, offset,
                    headerLength));
            headers.add(header);
            offset += headerLength;
        }
    }

    /**
     * fill packet specific fields by parsing the given data
     * 
     * @param data
     */
    protected abstract void parsePacketFields(final byte[] data);

    /**
     * calculate the lenght of all headers contained in the packet
     * 
     * @return get the total length of all headers
     */
    protected int getHeadersLength() {
        if (headers == null) {
            return 0;
        }
        Iterator iter = headers.iterator();
        if (iter == null) {
            return 0;
        }

        int length = 0;
        while (iter.hasNext()) {
            length += ((Header) iter.next()).getLength();
        }
        return length;
    }

    /**
     * override toString method
     */
    public String toString() {
        String result = "*************************************************\n";
        result += packetFieldsToString();
        result += "\n";
        if (headers != null) {
            result += "Headers:\n";
            result += headers.toString();
        }

        result += "*************************************************\n";
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Packet)) {
            return false;
        }

        Packet pkt = (Packet) obj;
        if (PacketLength != pkt.getPacketLength()) {
            return false;
        }

        if (headers == null) {
            return (pkt.getHeaders() == null);
        }

        Iterator thisIter = headers.iterator();
        Iterator pktIter = pkt.getHeaders();

        if (thisIter == null && pktIter == null) {
            return true;
        }
        if (thisIter == null && pktIter != null
                || thisIter != null && pktIter == null) {
            return false;
        }

        while (thisIter.hasNext()) {
            if (!pktIter.hasNext()) {
                /* in the case pktIter has fewer elements than thisIter */
                return false;
            }
            Header thisHeader = (Header) thisIter.next();
            Header pktHeader = (Header) pktIter.next();
            if (!thisHeader.equals(pktHeader)) {
                return false;
            }
        }

        if (pktIter.hasNext()) {
            /* in the case pktIer has more elements than thisIter */
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Arrays.hashCode(this.rawdata);
        return hash;
    }

    /**
     * give a human readable result for packet specific fields
     * 
     * @return String containing packet specific fields information
     */
    protected abstract String packetFieldsToString();
}
