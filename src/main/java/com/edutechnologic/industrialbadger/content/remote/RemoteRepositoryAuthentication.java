package com.edutechnologic.industrialbadger.content.remote;

/**
 * RemoteRepositoryAuthentication is responsible for authenticating
 * the user with a remote repository.
 */
public interface RemoteRepositoryAuthentication {
    /**
     * Authenticates the user with a remote repository.
     * @return true if the user is authenticated, false otherwise.
     */
    public boolean isAuthenticated();
}
