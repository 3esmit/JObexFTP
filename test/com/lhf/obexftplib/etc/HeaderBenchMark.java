/*
 * Created on Nov 17, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.lhf.obexftplib.etc;

import com.lhf.obexftplib.io.obexop.Header;
/**
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates 
 *
 * @author Joey Shen
 */
public final class HeaderBenchMark {
    public final static byte[] NAME_RAWDATA = {
            (byte)0x01, (byte)0x00, (byte)0x17, (byte)0x00, (byte)0x4a,
            (byte)0x00, (byte)0x75, (byte)0x00, (byte)0x6d, (byte)0x00, 
            (byte)0x61, (byte)0x00, (byte)0x72, (byte)0x00, (byte)0x2e, 
            (byte)0x00, (byte)0x74, (byte)0x00, (byte)0x78, (byte)0x00, 
            (byte)0x74, (byte)0x00, (byte)0x00
    };
    public final static byte[] NAME_VALUE = {
            (byte)0x00, (byte)0x4a, (byte)0x00, (byte)0x75, (byte)0x0, 
            (byte)0x6d, (byte)0x00, (byte)0x61, (byte)0x00, (byte)0x72, 
            (byte)0x00, (byte)0x2e, (byte)0x00, (byte)0x74, (byte)0x00, 
            (byte)0x78, (byte)0x00, (byte)0x74, (byte)0x00, (byte)0x00
    };
    public final static Header NAME_INSTANCE;
    
    public final static byte[] TYPE_RAWDATA = {
            (byte)0x42, (byte)0x00, (byte)0x1d, (byte)0x61, (byte)0x70,
            (byte)0x70, (byte)0x6c, (byte)0x69, (byte)0x63, (byte)0x61, 
            (byte)0x74, (byte)0x69, (byte)0x6f, (byte)0x6e, (byte)0x2f, 
            (byte)0x76, (byte)0x6e, (byte)0x64, (byte)0x2e, (byte)0x73, 
            (byte)0x79, (byte)0x6e, (byte)0x63, (byte)0x6d, (byte)0x6c, 
            (byte)0x2d, (byte)0x78, (byte)0x6d, (byte)0x6c
    };
    public final static byte[] TYPE_VALUE = {
            (byte)0x61, (byte)0x70, (byte)0x70, (byte)0x6c, (byte)0x69, 
            (byte)0x63, (byte)0x61, (byte)0x74, (byte)0x69, (byte)0x6f, 
            (byte)0x6e, (byte)0x2f, (byte)0x76, (byte)0x6e, (byte)0x64, 
            (byte)0x2e, (byte)0x73, (byte)0x79, (byte)0x6e, (byte)0x63, 
            (byte)0x6d, (byte)0x6c, (byte)0x2d, (byte)0x78, (byte)0x6d, 
            (byte)0x6c
    };
    public final static Header TYPE_INSTANCE;
    
    public final static byte[] CONNECTION_ID_RAWDATA = {
            (byte)0xcb, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01
    };
    public final static byte[] CONNECTION_ID_VALUE = {
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01
    };
    public final static Header CONNECTION_ID_INSTANCE;    
    
    public final static byte[] SESSION_SEQUENCE_NUMBER_RAWDATA = {
            (byte)0x93, (byte)0x01
    };
    public final static byte[] SESSION_SEQUENCE_NUMBER_VALUE = 
    {
            (byte)0x01
    };
    public final static Header SESSION_SEQUENCE_NUMBER_INSTANCE;   

    public final static int NUMBER = 4;
    public final static byte[][] RAWDATA = {NAME_RAWDATA, TYPE_RAWDATA, 
            CONNECTION_ID_RAWDATA, SESSION_SEQUENCE_NUMBER_RAWDATA};
    public final static byte[][]VALUE = {NAME_VALUE, TYPE_VALUE,
            CONNECTION_ID_VALUE, SESSION_SEQUENCE_NUMBER_VALUE};
    public final static byte[] TYPE = {Header.NAME, Header.TYPE,
            Header.CONNECTION_ID, Header.SESSION_SEQUENCE_NUMBER};
    public final static int[] VALUEOFFSET = {3, 3, 1, 1};
    public final static int[] DEFAULTLENGTH = {3, 3, 5, 2};
    public final static int[] LENGTH = {23, 29, 5, 2};
    public final static int[] VALUELENGTH = {20, 26, 4, 1};
    public final static Header[] INSTANCE = new Header[NUMBER];
    
    static {
        System.out.println("*********************************");
        System.out.println("* Header BenchMarks Initilizing *");
        System.out.println("*********************************");
        NAME_INSTANCE = new Header(Header.NAME);
        NAME_INSTANCE.setValue(NAME_VALUE);
        System.out.println("Name Header: " + NAME_INSTANCE);
        
        TYPE_INSTANCE = new Header(Header.TYPE);
        TYPE_INSTANCE.setValue(TYPE_VALUE);
        System.out.println("Type Header: " + TYPE_INSTANCE);
        
        CONNECTION_ID_INSTANCE = new Header(Header.CONNECTION_ID);
        CONNECTION_ID_INSTANCE.setValue(CONNECTION_ID_VALUE);
        System.out.println("Connection_Id Header: " + CONNECTION_ID_INSTANCE);
        
        SESSION_SEQUENCE_NUMBER_INSTANCE = new Header(
                Header.SESSION_SEQUENCE_NUMBER);
        SESSION_SEQUENCE_NUMBER_INSTANCE.setValue(
                SESSION_SEQUENCE_NUMBER_VALUE);
        System.out.println("Session_Sequance_Number Header: " + 
                SESSION_SEQUENCE_NUMBER_INSTANCE);
        
        INSTANCE[0] = NAME_INSTANCE;
        INSTANCE[1] = TYPE_INSTANCE;
        INSTANCE[2] = CONNECTION_ID_INSTANCE;
        INSTANCE[3] = SESSION_SEQUENCE_NUMBER_INSTANCE;
    }
}
