/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lhf.obex.dao;

import java.util.Hashtable;
import java.util.Iterator;

/**
 *
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
