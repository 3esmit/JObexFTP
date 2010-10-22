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
import com.lhf.obexftplib.io.obexop.GetRequest;
import com.lhf.obexftplib.io.obexop.Header;
import com.lhf.obexftplib.io.obexop.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public abstract class OBEXObject extends OutputStream {

    public static final OBEXFolder ROOT_FOLDER = new OBEXFolder(null, "a:/");
    private byte[] binaryName = {}, groupPerm = {'\"', '\"'}, userPerm = {'\"', '\"'};
    private Date modified = null, time = null;
    protected String name = "";
    private OBEXFolder parentFolder = ROOT_FOLDER;
    private ByteArrayOutputStream contents = new ByteArrayOutputStream(600);
    private boolean endOfBody = false;

    public abstract String getPath();

    public OBEXObject(OBEXFolder parentFolder) {
        this.parentFolder = parentFolder;
    }

    public void addResponse(Response res) {
        for (Iterator<Header> it = res.getHeaders(); it.hasNext();) {
            threatHeader(it.next());
        }
    }

    public void addResponses(Response[] res) {
        for (int i = 0; i < res.length; i++) {
            addResponse(res[i]);
        }
    }

    protected abstract void threatHeader(Header header);

    /**
     * @return the name
     */
    public byte[] getBinaryName() {
        return binaryName;
    }

    public String getName() {
        return name;
    }

    public boolean isReady() {
        return endOfBody;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
        this.binaryName = Utility.nameToBytes(name);
    }

    /**
     * @param name the name to set
     */
    public void setName(byte[] name) {
        this.binaryName = name;
        this.name = Utility.bytesToName(name);
    }

    /**
     * @return the modified
     */
    public Date getModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(Date modified) {
        this.modified = modified;
    }

    /**
     * @return the userPerm
     */
    public byte[] getUserPerm() {
        return userPerm;
    }

    /**
     * @param userPerm the userPerm to set
     */
    public void setUserPerm(boolean read, boolean write, boolean delete) {
        this.userPerm = Utility.buildPerm(read, write, delete, (byte) 0x38);
    }

    public void setUserPerm(String value) {
        if (value == null) {
            return;
        }
        this.userPerm = Utility.buildPerm(value, (byte) 0x38);
    }

    /**
     * @return the groupPerm
     */
    public byte[] getGroupPerm() {
        return groupPerm;
    }

    /**
     * TODO: Find the correct groupperm byte (its not 0x38) and add it at OBEXFile#getHeaderSet().
     * @param groupPerm the groupPerm to set
     */
    public void setGroupPerm(boolean read, boolean write, boolean delete) {
        this.groupPerm = Utility.buildPerm(read, write, delete, (byte) 0x38);
    }

    public void setGroupPerm(String value) {
        if (value == null) {
            return;
        }
        this.groupPerm = Utility.buildPerm(value, (byte) 0x38);
    }

    /**
     * @return the contents
     */
    public byte[] getContents() {
        return contents.toString().getBytes();
    }

    /**
     * @param contents the contents to set
     */
    public final void reset() {
        this.contents.reset();
        onReset();
        endOfBody = false;
    }

    protected void onReset() {
    }

    public void setContents(byte[] contents) throws IOException {
        if (endOfBody) {
            reset();
        }
        appendContent(contents);
    }

    private void appendContent(byte[] content) throws IOException {
        this.contents.write(content);
    }

    @Override
    public void write(int b) throws IOException {
        this.contents.write((char) b);
    }

    /**
     * @return the time
     */
    public Date getTime() {
        if (time == null) {
            time = new Date();
        }
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(Date time) {
        this.time = time;
    }

    public void setTime(String value) {
        if (value == null) {
            return;
        }
        this.time = Utility.getTime(value);
    }

    /**
     * @return the parentFolder
     */
    public OBEXFolder getParentFolder() {
        return parentFolder;
    }

    /**
     * @param parentFolder the parentFolder to set
     */
    private void setParentFolder(OBEXFolder parentFolder) {
        this.parentFolder = parentFolder;
    }

    public final void setReady() {
        this.endOfBody = true;
        onReady();
    }

    protected void onReady() {
    }

    @Override
    public boolean equals(Object compobj) {
        if (compobj instanceof OBEXObject) {
            OBEXObject obj = (OBEXObject) compobj;
            return (this.modified == obj.modified) && (this.time == obj.time) && Utility.compareBytes(this.binaryName, obj.binaryName);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Utility.listingFormat(builder, name, getSizeString(), getTime());
        return builder.toString();
    }

    public abstract String getSizeString();

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Arrays.hashCode(this.binaryName);
        hash = 29 * hash + (this.modified != null ? this.modified.hashCode() : 0);
        hash = 29 * hash + (this.time != null ? this.time.hashCode() : 0);
        return hash;
    }
}
