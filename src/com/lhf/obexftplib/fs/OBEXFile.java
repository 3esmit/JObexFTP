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
import com.lhf.obexftplib.io.obexop.PutRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Class for encapsulating data and controlling the flow of file objects, used for put and get operations.
 * Get operations usually fill all the available data in server, and set the contents as String, that can be obtained with getContents() or getInputStream();
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public class OBEXFile extends OBEXObject {

    private byte[] size;
    private OBEXFolder parentFolder;
    private InputStream inputStream;
    private String path = null;

    public OBEXFile(OBEXFolder parentFolder, String filename) {
        super(parentFolder);
        setName(filename);
    }

    public OBEXFile(String filename) {
        this(ROOT_FOLDER, filename);
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

        Header appParameters = new Header(Header.APP_PARAMETERS);
        appParameters.setValue(getUserPerm());
        hs.add(appParameters);
        return hs;
    }

    /**
     * Sets the contents of object.
     * @param contents
     * @see JOBEXFile#getInputStream()
     * @see JOBEXFile#setContents(java.lang.String) ()
     */
    @Override
    public void setContents(byte[] contents) throws IOException {
        setSize(contents.length);
        super.setContents(contents);
    }

    /**
     * Function used to determinate the path of the getted object, when a file is downloaded from server. Not used for put operations.
     * @return the path of getted object.
     */
    @Override
    public String getPath() {
        if (path != null) {
            return path + "/" + getBinaryName();
        } else {
            return parentFolder.getPath() + "/" + getBinaryName();
        }
    }

    /**
     * @return the size of the contents of object
     */
    public byte[] getSize() {
        return size;
    }

    /**
     * @param size the size of the contents.
     */
    public void setSize(int size) {
        this.size = Utility.intToBytes(size, 4);
    }

    /**
     * Method used to get the inputstream to read the contents of object.  If is null, will create a ByteArrayInputStream with the object contents, setted by setContent(String s);
     * @return the inputStream to read the contents of the file.
     * @see JOBEXObject#setContents(java.lang.String)
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
    public void setInputStream(InputStream inputStream) {
        try {
            setSize(inputStream.available());
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
    public boolean equals(Object compobj) {
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
    protected void threatHeader(Header header) {
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
                }
                break;
            case Header.END_OF_BODY:
                try {
                    setContents(header.getValue());
                } catch (IOException ex) {
                }
                setReady();
                break;
        }
    }
}
