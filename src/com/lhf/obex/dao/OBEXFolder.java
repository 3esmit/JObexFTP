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
package com.lhf.obex.dao;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * Reference object to folder obexobject in server
 * @author ricardo
 */
public class OBEXFolder extends OBEXObject {

    private Hashtable<String, OBEXFile> files = null;
    private Hashtable<String, OBEXFolder> folders = null;
    private OBEXFolder parent = null;

    private OBEXFolder(String name) {
        setName(name);
    }

    public OBEXFolder() {
    }

    public boolean isRoot() {
        if (getName() == null) {
            return true;
        } else {
            return false;
        }
    }

    public void addFolder(OBEXFolder folder) {
        getFolders().put(folder.getName(), folder);
        folder.setParent(this);
    }

    public OBEXFolder addFolder(String name) {
        OBEXFolder folder = new OBEXFolder(name);
        getFolders().put(name, new OBEXFolder(name));
        return folder;
    }

    public OBEXFolder getFolder(String name) {
        return getFolders().get(name);
    }

    public void addFile(OBEXFile file) {
        getFiles().put(file.getName(), file);
        file.setFolder(this);
    }

    public OBEXFile addFile(String name) {
        OBEXFile file = new OBEXFile(name);
        addFile(file);
        return file;
    }

    public OBEXFile getFile(String name) {
        return getFiles().get(name);
    }

    public String[] getFolderListingArray() {
        String[] s = new String[getFolders().size() + getFiles().size()];
        Iterator iter = getFolders().keySet().iterator();
        int i;
        for (i = 0; iter.hasNext(); i++) {
            s[i] = (String) iter.next();
        }
        iter = getFiles().keySet().iterator();
        for (; iter.hasNext(); i++) {
            s[i] = (String) iter.next();
        }
        return s;
    }

    /**
     * @return the files
     */
    public Hashtable<String, OBEXFile> getFiles() {
        if (files == null) {
            files = new Hashtable<String, OBEXFile>();
        }
        return files;
    }

    /**
     * @return the folders
     */
    public Hashtable<String, OBEXFolder> getFolders() {
        if (folders == null) {
            folders = new Hashtable<String, OBEXFolder>();
        }
        return folders;
    }

    /**
     * @return the parent
     */
    public OBEXFolder getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    void setParent(OBEXFolder parent) {
        this.parent = parent;
    }
}
