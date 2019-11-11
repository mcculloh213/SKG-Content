package com.edutechnologic.industrialbadger.content.remote.exception;

/**
 * RemoteRetrievalException represents any exception that occurs during the remote retrieval
 * process.
 */
public class RemoteRetrievalException extends Exception {

    /**
     * Creates a remote retrieval exception which wraps an underlying exception
     * @param e The underlying exception to wrap
     */
    public RemoteRetrievalException(Exception e) {
        super(e);
    }
}
