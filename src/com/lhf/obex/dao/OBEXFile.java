/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lhf.obex.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 *
 * @author ricardo
 */
public class OBEXFile extends OBEXObject {

    private InputStream is = null;
    private long size = 0;
    private OBEXFolder folder = null;
    public OBEXFile(String name) {
        name.replace('\\', '/');
        StringTokenizer tok = new StringTokenizer(name, "/");
        String fileNameWithoutPath = "";
        while (tok.hasMoreTokens()) {
            fileNameWithoutPath = tok.nextToken();
        }
        setName(fileNameWithoutPath);
    }

    public OBEXFile(InputStream is, String name) {
        this(name);
        this.is = is;
    }

    /**
     * @return the size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the fileStream
     */
    public InputStream getInputStream() {
        return is;
    }

    /**
     * @param fileStream the fileStream to set
     */
    public void setInputStream(InputStream is) {
        this.is = is;
    }

    /**
     * Returns an estimate of the number of remaining bytes that can be read (or skipped over) from this input stream without blocking by the next invocation of a method for this input stream. The next invocation might be the same thread or another thread. A single read or skip of this many bytes will not block, but may read or skip fewer bytes.
     * In some cases, a non-blocking read (or skip) may appear to be blocked when it is merely slow, for example when reading large files over slow networks.
     * @return the avaliable bytes
     * @throws IOException
     */
    public int available() throws IOException {
        return is.available();
//        if (fileStream != null) {
//            return fileStream.available();
//        } else {
//            return byteis.available();
//        }
    }

    /**
     * Reads a byte of data from this input stream. This method blocks if no input is yet available.
     * @return the next byte of data, or -1 if the end of the file is reached.
     * @throws IOException if an I/O error occurs.
     */
    public int read() throws IOException {
//        if (fileStream != null) {
        return is.read();
//        } else {
//            return byteis.read();
//        }
    }

    /**
     * Reads up to b.length bytes of data from this input stream into an array of bytes. This method blocks until some input is available.
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the file has been reached.
     * @throws IOException if an I/O error occurs
     */
    public int read(byte[] b) throws IOException {
//        if (fileStream != null) {
        return is.read(b);
//        } else {
//            return byteis.read(b);
//        }
    }

    /**
     * Reads up to len bytes of data from this input stream into an array of bytes. If len is not zero, the method blocks until some input is available; otherwise, no bytes are read and 0 is returned.
     * @param b the buffer into which the data is read.
     * @param off the start offset in the destination array b
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached.
     * @throws IOException if an I/O error occurs
     */
    public int read(byte[] b, int off, int len) throws IOException {
//        if (fileStream != null) {
        return is.read(b, off, len);
//        } else {
//            return byteis.read(b, off, len);
//        }
    }

    /**
     * Skips over and discards n bytes of data from the input stream.
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException  if n is negative, if the stream does not support seek, or if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
//        if (fileStream != null) {
        return is.skip(n);
//        } else {
//            return byteis.skip(n);
//        }
    }

    /**
     * Closes this file input stream and releases any system resources associated with the stream.
     * @throws IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
//        if (fileStream != null) {
        is.close();
//        } else {
//            byteis.close();
//        }
    }

    /**
     * @return the folder
     */
    public OBEXFolder getFolder() {
        return folder;
    }

    /**
     * @param folder the folder to set
     */
    void setFolder(OBEXFolder folder) {
        this.folder = folder;
    }

}
