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
import com.lhf.obexftplib.io.obexop.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

/**
 * Abstract class containing common attributes and behavior of OBEXFile and OBEXFolder.
 * @author Ricardo Guilherme Schmidt <ricardo@lhf.ind.br>
 */
public abstract class OBEXObject {

    public static final OBEXFolder ROOT_FOLDER = new OBEXFolder(null, "a:");
    private final ByteArrayOutputStream contents = new ByteArrayOutputStream(600);
    private final OBEXFolder parentFolder; //parent folder cannot be changed.
    private byte[] binaryName = {};
    String groupPerm = "", userPerm = "";
    private Date modified = null, time = null;
    private boolean endOfBody = false;
    private String path = null;
    protected String name = "";

    public String getPath() {
        if (path == null) {
            if (getParentFolder() == null) {
                path = "";
            } else {
                path = getParentFolder().getPath() + "/";
            }
        }
        return path + name;
    }

    public OBEXObject(final OBEXFolder parentFolder, final String name) {
        this.parentFolder = parentFolder;
        setName(name);
    }

    public void addResponse(final Response res) {
        for (Iterator<Header> it = res.getHeaders(); it.hasNext();) {
            threatHeader(it.next());
        }
    }

    public void addResponses(final Response[] res) {
        reset();
        for (int i = 0; i < res.length; i++) {
            addResponse(res[i]);
        }
    }

    protected void onReady() {
    }

    protected void onReset() {
    }

    protected abstract void threatHeader(final Header header);

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
    public final void setName(final String name) {
        this.name = name;
        this.binaryName = Utility.nameToBytes(name);
    }

    /**
     * @param name the name to set
     */
    public final void setName(final byte[] name) {
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
    public void setModified(final Date modified) {
        this.modified = modified;
    }

    /**
     * @return the userPerm
     */
    public String getUserPerm() {
        return userPerm;
    }

    /**
     * @param userPerm the userPerm to set
     */
    public void setUserPerm(final boolean read, final boolean write, final boolean delete) {
        this.userPerm = (read ? "R" : "") + (write ? "W" : "") + (delete ? "D" : "");
    }

    public void setUserPerm(final String value) {
        if (value == null) {
            return;
        }
        this.userPerm = value;
    }

    /**
     * @return the groupPerm
     */
    public String getGroupPerm() {
        return groupPerm;
    }

    /**
     * TODO: Find the correct groupperm byte (its not 0x38) and add it at OBEXFile#getHeaderSet().
     * @param groupPerm the groupPerm to set
     */
    public void setGroupPerm(final boolean read, final boolean write, final boolean delete) {
        this.groupPerm = (read ? "R" : "") + (write ? "W" : "") + (delete ? "D" : "");
    }

    public void setGroupPerm(final String value) {
        if (value == null) {
            return;
        }
        this.groupPerm = value;
    }

    /**
     * @return the contents
     */
    public byte[] getContents() {
        return contents.toByteArray();
    }

    /**
     * @param contents the contents to set
     */
    public final void reset() {
        this.contents.reset();
        onReset();
        endOfBody = false;
    }

    /**
     * Resets and sets the content of this object.
     * @param contents
     * @throws IOException
     */
    protected void setContents(final byte[] contents) throws IOException {
        if (endOfBody) {
            reset();
        }
        appendContent(contents);
    }

    private void appendContent(final byte[] content) throws IOException {
        this.contents.write(content);
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
    public void setTime(final Date time) {
        this.time = time;
    }

    /**
     * The time to set in yyyyMMdd'T'HHmmss time representated String
     * @param value
     */
    public void setTime(final String value) {
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

    public final void setReady() {
        this.endOfBody = true;
        onReady();
    }

    @Override
    public boolean equals(final Object compobj) {
        if (compobj instanceof OBEXObject) {
            OBEXObject obj = (OBEXObject) compobj;
            return (this.modified == obj.modified) && (this.time == obj.time) && Utility.compareBytes(this.binaryName, obj.binaryName);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Utility.listingFormat(builder, name, getSizeString(), getTime(), getUserPerm(), getGroupPerm());
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
