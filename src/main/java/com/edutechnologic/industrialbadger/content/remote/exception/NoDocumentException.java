package com.edutechnologic.industrialbadger.content.remote.exception;

/**
 * NoDocumentException represents any case where an action
 * is performed on a storage repository where the document is assumed
 * to exist locally when it does not.
 */
public class NoDocumentException extends Exception {

    /**
     * Creates an exception with the given error message.
     * @param message The error message give this exception.
     */
    public NoDocumentException(String message) {
        super(message);
    }
}