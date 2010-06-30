/**
 *     This file is part of JObexFTP.
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
package com.lhf.obex;

import com.lhf.obex.dao.OBEXFile;

/**
 * Basic structure of OBEX client
 * @author Ricardo Guilherme Schmidt, Florian Chiș
 */
public interface OBEXInterface {
    
    //----------OPERATION CODES-------------------------------------------------
    static final int OBEX_CONNECT = 0x80; //connect
    static final int OBEX_DISCONNECT = 0x81; //disconnect
    static final int OBEX_FORMAT = 0x04; //format filesystem
    static final int OBEX_DELETE = 0x00; //delete file
    static final int OBEX_CHANGE_PERMISSION = 0x01; //change file permisions
    static final int OBEX_PUT = 0x02; //put operation
    static final int OBEX_PUT_NEXT = 0x03; //put next chunck
    static final int OBEX_PUT_FINAL = 0x82; //final chunk
    static final int OBEX_GET = 0x83; //get operation
    static final int OBEX_ABORT = 0x84; //abort
    static final int OBEX_SET_PATH = 0x85; //set path
    static final int OBEX_SET_PATH2 = 0x86;
    //----------RESPONSE CODES--------------------------------------------------
    static final int OBEX_CONTINUE = 0x90; //next
    static final int OBEX_OK = 0xA0; //all done
    static final int OBEX_BAD_REQUEST = 0xC0;
    static final int OBEX_UNAUTHORIZED = 0xC1;
    static final int OBEX_FORBIDDEN = 0xC3;
    //----------OBEX HEADER IDENTIFIERS-----------------------------------------
    static final int OBEX_HI_TYPE = 0x42; //object type
    static final int OBEX_HI_TIME = 0x44; //time of modification
    static final int OBEX_HI_LENGTH = 0xC3; //obect length
    static final int OBEX_HI_BODY = 0x48; //chunck of object body
    static final int OBEX_HI_END_OF_BODY = 0x49; //end of body
    //----------INSTRUCTIONS----------------------------------------------------
    static final byte[] OBEX_OPERATION_CONNECT = {(byte) 0x80, (byte) 00, (byte) 0x1a, (byte) 0x10, (byte) 00, (byte) 0x40, 6, (byte) 0x46, (byte) 00, (byte) 0x13, (byte) 0x6B, 1, (byte) 0xCB, (byte) 0x31, (byte) 0x41, 6, (byte) 0x11, (byte) 0xD4, (byte) 0x9A, (byte) 0x77, 0, (byte) 0x50, (byte) 0xDA, (byte) 0x3F, (byte) 0x47, (byte) 0x1f};
    static final byte[] OBEX_OPERATION_FORMAT = {(byte) 0x82, (byte) 0x00, (byte) 0x08, (byte) 0x4c, (byte) 0x00, (byte) 0x05, (byte) 0x31, (byte) 0x00};
    //----------OTHER-----------------------------------------------------------
    static final int FLOW_NONE = 0;
    static final int FLOW_XONXOFF = 1;
    static final int FLOW_RTSCTS = 2;

    /**
     * Changes the actual path in the obexconnection
     * @param path to be changed
     * @param backwards if your going back in dirs (not tested yet)
     * @param create if you want to create a new dir if it not exsits
     * @return array of integers with the obex response
     * @throws OBEXException if operation was not successful
     */
    byte[] setPath(String path, boolean backwards, boolean create) throws OBEXException;

    /**
     * function for obex get operations
     * @param what you want to get
     * @return what you got
     * @throws OBEXException if operation was not successful
     */
    byte[] get(byte[] what) throws OBEXException;

    /**
     * function for obex put operation
     * @param type of the put operation you want to realize
     * @param object the object you want to send or change settings
     * @return true if could put, false if not.
     * @throws OBEXException if operation was not successful
     */
    boolean put(int type, OBEXFile object) throws OBEXException;

    /**
     * function to abort obex operations
     * @return the obex response
     * @throws OBEXException if operation was not successful
     */
    byte[] abort() throws OBEXException;

    /**
     * function to format the flash filesystem
     * @return true if the format was successful
     * @throws OBEXException if the operation was not successful
     */
    boolean format() throws OBEXException;

    /**
     * function to open a obex connection
     * @return true if connection was successful
     * @throws OBEXException if operation was not successful
     */
    boolean enterOBEXMode() throws OBEXException;

    /**
     * function to leaveOBEXMode from obex server
     * @return true if disconnection was accepted
     * @throws OBEXException if operation was not successful
     */
    boolean leaveOBEXMode() throws OBEXException;
}
