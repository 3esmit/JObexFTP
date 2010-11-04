/**
 * Created on Sep 21, 2004
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
 * abstract class for all OBEX responses
 * 
 * @author joey
 */
public abstract class Response extends Packet {

    public static final byte SUCCESS = (byte) 0x20;
    public static final byte CONTINUE = (byte) 0x10;
    public static final byte CREATED = (byte) 0x21;
    public static final byte BADREQUEST = (byte) 0x40;
    public static final byte FINAL = (byte) 0x80;
    protected byte respcode;

    /**
     * get the response type
     */
    public byte getType() {
        return respcode;
    }

    /**
     * set the response type
     * 
     * @param atype
     */
    public void setType(final byte atype) {
        respcode = atype;
    }

    /**
     * set the final bit of the connect response
     */
    public void setFinal() {
        respcode |= FINAL;
    }

    /**
     * detect whether the final bit has been set
     * 
     * @return true if the final bit was set, otherwise false
     */
    public boolean isFinal() {
        return ((respcode & FINAL) == FINAL);
    }

    @Override
    public boolean equals(final Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        if (obj instanceof Response) {
            Response resp = (Response) obj;
            return respcode == resp.getType();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 41 * hash + this.respcode;
        return hash;
    }
}
