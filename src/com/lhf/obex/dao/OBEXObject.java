/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lhf.obex.dao;

/**
 *
 * @author Ricardo Guilherme Schmidt
 */
public class OBEXObject {

    private String name, modified, userPerm, groupPerm;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the modified
     */
    public String getModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(String modified) {
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
    public void setUserPerm(String userPerm) {
        this.userPerm = userPerm;
    }

    /**
     * @return the groupPerm
     */
    public String getGroupPerm() {
        return groupPerm;
    }

    /**
     * @param groupPerm the groupPerm to set
     */
    public void setGroupPerm(String groupPerm) {
        this.groupPerm = groupPerm;
    }
}
