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

/**
 * Basic obexobject, common methods and parameters to files and folders.
 * @author Ricardo Guilherme Schmidt
 */
public abstract class OBEXObject {

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
