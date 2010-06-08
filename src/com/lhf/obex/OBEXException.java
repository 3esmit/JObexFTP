package com.lhf.obex;

import java.io.IOException;

/**
 *
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
