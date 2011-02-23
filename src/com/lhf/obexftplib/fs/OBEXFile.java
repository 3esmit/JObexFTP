/**
 * Last updated in 21/Out/2010
 *
 *    This file is part of JObexFTP.
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
package com.lhf.obexftplib.fs;

import com.lhf.obexftplib.etc.Utility;
import com.lhf.obexftplib.io.obexop.Header;
import com.lhf.obexftplib.io.obexop.HeaderSet;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Class for encapsulating data and controlling the flow of file objects, used for put and get operations.
 * Get operations usually fill all the available data in server, and set the contents as String, that can be obtained with getContents() or getInputStream();
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public final class OBEXFile extends OBEXObject {

    private byte[] size = null;
    private InputStream inputStream;

    public OBEXFile(final OBEXFolder parentFolder, final String filename) {
        super(parentFolder, filename);
    }

    public OBEXFile(final String filename) {
        super(ROOT_FOLDER, filename);
    }

    public HeaderSet getHeaderSet() {
        HeaderSet hs = new HeaderSet();

        Header nameheader = new Header(Header.NAME);
        nameheader.setValue(getBinaryName());
        hs.add(nameheader);

        Header lenght = new Header(Header.LENGTH);
        lenght.setValue(getSize());
        hs.add(lenght);

        Header time = new Header(Header.TIME);
        time.setValue(Utility.getTime(getTime()).getBytes());
        hs.add(time);

        return hs;
    }

    /**
     * Sets the contents of object.
     * @param contents
     * @see JOBEXFile#getInputStream()
     * @see JOBEXFile#setContents(java.lang.String) ()
     */
    @Override
    protected void setContents(final byte[] contents) throws IOException {
        setSize(contents.length);
        super.setContents(contents);
    }

    /**
     * @return the size of the contents of object
     */
    public byte[] getSize() {
        if (size == null) {
            setSize(getContents().length);
        }
        return size;
    }

    /**
     * @param size the size of the contents.
     */
    public void setSize(final int size) {
        this.size = Utility.intToBytes(size, 4);
    }

    /**
     * Method used to get the inputstream to read the contents of object.
     * Should be used only internally by JObexFTP to read the file to be written
     * @return the inputStream to read the contents of the file.
     * @see JOBEXObject#getContents(java.lang.String)
     */
    public InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new ByteArrayInputStream(this.getContents());
        }
        return inputStream;
    }

    /**
     * Method for setting inputstream for put send operations. If setted, the client will use your inputstream, reather then the contents defined with setContents
     * @param inputStream the inputStream used for reading the contents of the file. Inputstream must be ready to use when setted.
     * @see JOBEXObject#setContents(java.lang.String)
     */
    public void setInputStream(final InputStream inputStream) {
        try {
            if (inputStream != null) {
                setSize(inputStream.available());
            }
        } catch (IOException ex) {
            //TODO: Log errors.
        }
        this.inputStream = inputStream;
    }

    @Override
    public String getSizeString() {
        return Utility.humanReadableByteCount(Utility.bytesToInt(getSize()), true);
    }

    @Override
    public boolean equals(final Object compobj) {
        if (super.equals(compobj) && compobj instanceof OBEXFile) {
            OBEXFile file = (OBEXFile) compobj;
            return (file.size == this.size);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 31 * hash + Arrays.hashCode(this.size);
        return hash;
    }

    @Override
    protected void threatHeader(final Header header) {
        switch (header.getId()) {
            case Header.LENGTH:
                this.size = header.getValue();
                break;
            case Header.NAME:
                setName(header.getValue());
                break;
            case Header.BODY:
                try {
                    setContents(header.getValue());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            case Header.END_OF_BODY:
                try {
                    setContents(header.getValue());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                setReady();
                break;
            default:
                System.out.println("strange header: " +Utility.dumpBytes(header.toBytes()));
        }
    }

    @Override
    protected void onReady() {
        this.setSize(getContents().length);
    }
}
