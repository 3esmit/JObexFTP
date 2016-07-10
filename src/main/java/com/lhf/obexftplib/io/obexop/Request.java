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

/**
 *
 * abstract class for all the OBEX requests
 * 
 * @author Joey Shen
 */
public abstract class Request extends Packet {

    public static final byte CONNECT = (byte) 0x80;
    public static final byte DISCONNECT = (byte) 0x81;
    public static final byte PUT = (byte) 0x02;
    public static final byte GET = (byte) 0x03;
    public static final byte SETPATH = (byte) 0x85;
    public static final byte SETPATH2 = (byte) 0x86;
    public static final byte SESSION = (byte) 0x87;
    public static final byte ABORT = (byte) 0xFF;
    public static final byte FINAL = (byte) 0x80;
    protected byte opcode;

    /**
     * set request type
     * 
     * @param type
     */
    public void setType(final byte type) {
        opcode = type;
    }

    /**
     * get request type
     * 
     * @return type of the request
     */
    public byte getType() {
        return opcode;
    }

    /**
     * set the final bit of the request
     */
    public void setFinal() {
        opcode |= FINAL;
    }

    /**
     * detect whether the request is the final packet
     * 
     * @return true if the final bit was set
     */
    public boolean isFinal() {
        return ((opcode & FINAL) == FINAL);
    }

    /**
     * get Header Value
     * 
     * @return header value byte array
     */
    @Override
    public byte[] getHeaderValue(final byte headerType) {
        return headers.getHeaderValue(headerType);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof Request) {

            Request req = (Request) obj;

            return opcode == req.getType();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 43 * hash + this.opcode;
        return hash;
    }
}
