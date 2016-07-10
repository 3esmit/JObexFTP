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
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Class represents for OBEX header
 * 
 * @author Joey Shen (joey.shen@sun.com)
 */
public class Header {

    /* Header Names, see OBEX specification 2.2 */
    public static final byte COUNT = (byte) 0xC0;
    public static final byte NAME = (byte) 0x01;
    public static final byte TYPE = (byte) 0x42;
    public static final byte LENGTH = (byte) 0xC3;
    public static final byte TIME = (byte) 0x44;
    public static final byte DESCRIPTION = (byte) 0x05;
    public static final byte TARGET = (byte) 0x46;
    public static final byte HTTP = (byte) 0x47;
    public static final byte BODY = (byte) 0x48;
    public static final byte END_OF_BODY = (byte) 0x49;
    public static final byte WHO = (byte) 0x4A;
    public static final byte CONNECTION_ID = (byte) 0xCB;
    public static final byte APP_PARAMETERS = (byte) 0x4C;
    public static final byte AUTH_CHALLENGE = (byte) 0x4D;
    public static final byte AUTH_RESPONSE = (byte) 0x4E;
    public static final byte CREATOR_ID = (byte) 0xCF;
    public static final byte WAN_UUID = (byte) 0x50;
    public static final byte OBJECT_CLASS = (byte) 0x51;
    public static final byte SESSION_PARAMETERS = (byte) 0x52;
    public static final byte SESSION_SEQUENCE_NUMBER = (byte) 0x93;
    private byte HeaderId;
    private int HeaderLength = 0;
    private byte[] HeaderValue;

    public Header() {
    }

    /**
     * construct header of certain type 
     * 
     * @param type
     */
    public Header(final byte type) {
        HeaderId = type;
        if (((int) type & 0xC0) == 0xC0) {
            HeaderLength = 5;
        } else if (((int) type & 0xC0) == 0x80) {
            HeaderLength = 2;
        } else {
            HeaderLength = 3;
        }
    }

    /**
     * construct header from rawdata
     * 
     * @param header
     */
    public Header(final byte[] header) {
        int offset = 3;
        HeaderId = header[0];

        /*
         * get the actual length instead of parsing from the rawdata
         */
        HeaderLength = header.length;
        if (!hasLengthField(header[0])) {
            offset = 1;
        }

        HeaderValue = new byte[header.length - offset];
        for (int i = 0; i < header.length - offset; i++) {
            HeaderValue[i] = header[i + offset];
        }
    }

    /**
     * get the length according to the given Header Id. if the lenght is fixed,
     * return -1
     * 
     * @param type HeaderId
     * @return the length of given header type, -1 if the length is fixed.
     */
    public static int getHeaderLength(final byte type) {
        int result = -1;
        switch ((int) type & 0xC0) {
            case 0x80:
                result = 2;
                break;
            case 0xC0:
                result = 5;
                break;
            default:
        }
        return result;
    }

    /**
     * get HeaderId
     * 
     * @return HeaderId
     */
    public byte getId() {
        return HeaderId;
    }

    /**
     * set HeaderId
     * 
     * @param type
     */
    public void setId(final byte type) {
        HeaderId = type;
    }

    /**
     * get length of the header
     * 
     * @return length of the header
     */
    public int getLength() {
        return HeaderLength;
    }

    /**
     * get Header Value
     * 
     * @return Header Value
     */
    public byte[] getValue() {
        return HeaderValue;
    }

    /**
     * set Header Value
     * 
     * @param value
     */
    public void setValue(final byte[] value) {
        HeaderValue = value;
        if (hasLengthField(HeaderId)) {
            HeaderLength += value.length;
        }
    }

    /**
     * convert the whole header to rawdata
     * 
     * @return rawdata
     */
    public byte[] toBytes() {
        byte[] rawdata = new byte[HeaderLength];
        rawdata[0] = HeaderId;
        int offset = 1;

        if (hasLengthField(HeaderId)) {
            byte[] length = Utility.intToBytes(HeaderLength, 2);
            Utility.setBytes(rawdata, length, 1, 2);
            offset = 3;
        }

        for (int i = 0; i < HeaderValue.length; i++) {
            rawdata[offset + i] = HeaderValue[i];
        }

        return rawdata;
    }

    /**
     * override toString method
     */
    @Override
    public String toString() {
        String result = "";
        result += "Header: " + getHeaderName() + "\n";
        result += "Length: " + HeaderLength + "\n";
        result += "Value: ";

        if (((int) HeaderId & 0xC0) == 0) {
            try {
                result += new String(Utility.getBytes(HeaderValue, 0,
                        HeaderValue.length - 2), "UTF-16BE");
            } catch (UnsupportedEncodingException ueE) {
                ueE.printStackTrace();
            }
        } else {
            if (HeaderId == Header.BODY) {
                result += (HeaderLength - 3) + " body bytes omitted";
            } else {
                for (int i = 0; i < HeaderValue.length; i++) {
                    result += Utility.byteToHexString(HeaderValue[i])
                            + ", ";
                }
            }
        }
        result += "\n";

        return result;
    }

    /**
     * get the name of the Header as String
     * 
     * @return name of the header
     */
    private String getHeaderName() {
        String result = "UNKNOWN";

        switch (HeaderId) {
            case Header.COUNT:
                result = "COUNT";
                break;
            case Header.NAME:
                result = "NAME";
                break;
            case Header.BODY:
                result = "BODY";
                break;
            case Header.END_OF_BODY:
                result = "END_OF_BODY";
                break;
            case Header.TARGET:
                result = "TARGET";
                break;
            case Header.TYPE:
                result = "TYPE";
                break;
            case Header.TIME:
                result = "TIME";
                break;
            case Header.APP_PARAMETERS:
                result = "APP_PARAMETERS";
                break;
            case Header.AUTH_CHALLENGE:
                result = "AUTH_CHALLENGE";
                break;
            case Header.AUTH_RESPONSE:
                result = "AUTH_RESPONSE";
                break;
            case Header.CONNECTION_ID:
                result = "CONNECTION_ID";
                break;
            case Header.CREATOR_ID:
                result = "CREATOR_ID";
                break;
            case Header.DESCRIPTION:
                result = "DESCRIPTION";
                break;
            case Header.HTTP:
                result = "HTTP";
                break;
            case Header.LENGTH:
                result = "LENGTH";
                break;
            case Header.OBJECT_CLASS:
                result = "OBJECT_CLASS";
                break;
            case Header.SESSION_PARAMETERS:
                result = "SESSION_PARAMETERS";
                break;
            case Header.SESSION_SEQUENCE_NUMBER:
                result = "SESSION_SEQUENCE_NUMBER";
                break;
            case Header.WHO:
                result = "WHO";
                break;
            default:
            /*
             * keep the result as UNKNOWN
             */
        }

        return result;
    }

    /**
     * override the equals methods of Object
     * 
     * @return true if the content of the given obj is the same as this Header
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Header) {
            Header inHeader = (Header) obj;
            if (inHeader.getId() != HeaderId) {
                return false;
            }
            if (inHeader.getLength() != HeaderLength) {
                return false;
            }
            return Utility.compareBytes(inHeader.getValue(), HeaderValue);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.HeaderId;
        hash = 97 * hash + this.HeaderLength;
        hash = 97 * hash + Arrays.hashCode(this.HeaderValue);
        return hash;
    }

    /**
     * detect if the header contains HeaderLength field
     * 
     * @return true if it contains (in case highest bit as 0), false if not
     * (in case highest bit as 1)
     */
    protected static boolean hasLengthField(final byte type) {
        return (((int) type) & 0x80) == 0;
    }
}
