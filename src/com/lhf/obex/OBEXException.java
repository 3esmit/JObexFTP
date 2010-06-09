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
package com.lhf.obex;

import java.io.IOException;

/**
 * OBEXException a class to indicate obex communication problems.
 * @author Ricardo Guilherme Schmidt
 */
public class OBEXException extends IOException {

    /**
     * Constructs an IOException with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause). This constructor is useful for IO exceptions that are little more than wrappers for other throwables.
     * @param message
     * @param cause
     */
    public OBEXException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }

    /**
     * Constructs an IOException with the specified detail message.
     * @param message
     */
    public OBEXException(String message) {
        super(message);
    }

    /**
     * Constructs an IOException with the specified cause and a detail message of (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause). This constructor is useful for IO exceptions that are little more than wrappers for other throwables.
     * @param cause
     */
    public OBEXException(Throwable cause) {
        super();
        this.initCause(cause);
    }

    /**
     * Constructs an IOException with null as its error detail message.
     */
    public OBEXException() {
        super();
    }
}
