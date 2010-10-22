/*
 * Created on Nov 30, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.lhf.obexftplib.etc;

/**
 * BenchMarks to test OBEXUtility 
 *
 * @author Joey Shen
 */
public final class UtilityBenchMark {
    public static final int NUMBER = 4;
    
    public static final int[] INTEGERS = {
            0x02, 0xdc34, 0x85a351, 0xdf534B35 
    };
    
    public static final int[] LENGTHS = {
            2, 2, 4, 4
    };
    
    public static final byte[] BYTES = {
        (byte)0x02, (byte)0xdc, (byte)0xa3, (byte)0x35
    };
    
    public static final String[] BYTESTRINGS = {
        "0x02", "0xdc", "0xa3", "0x35"
    };
    
    public static final byte[][] BYTEARRAYS = {
            {
                (byte)0x00, (byte)0x02
            },
            {
                (byte)0xdc, (byte)0x34
            },
            {
                (byte)0x00, (byte)0x85, (byte)0xa3, (byte)0x51
            },
            {
                (byte)0xdf, (byte)0x53, (byte)0x4b, (byte)0x35
            }
    };
}
