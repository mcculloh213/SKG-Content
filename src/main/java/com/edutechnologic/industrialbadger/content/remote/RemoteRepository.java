package com.edutechnologic.industrialbadger.content.remote;

import com.edutechnologic.industrialbadger.base.provider.RemoteDocumentProvider;
import com.edutechnologic.industrialbadger.content.remote.exception.NoDocumentException;
import com.edutechnologic.industrialbadger.content.remote.exception.RemoteRetrievalException;

import java.util.Set;

import androidx.annotation.NonNull;

import ktx.sovereign.database.entity.Document;

/**
 * RemoteRepository encapsulates all interactions with a non-local repository.
 */
public interface RemoteRepository {

    /**
     * Retrieves the authentication provider for this remote repository.
     * @return The remote repository authenticator.
     */
    RemoteRepositoryAuthentication getAuthenticator();

    /**
     * Retrieves the enum value for files stored within this remote repository.
     * @return The remote repository source.
     */
    RemoteDocumentProvider.RemoteSource getSource();

    /**
     * Executes a search within the remote repository with the given query or document ids.
     * @param query The query to execute the search.
     * @return An iterable of all documents which matched that search.
     * @throws RemoteRetrievalException if an exception occurs during the search process.
     */
    Iterable<Document> search(@NonNull String query) throws RemoteRetrievalException;

    /**
     * Retrieves the contents of a file as a byte array.
     * @param remote the document id which represents a remote file.
     * @return The contents of the file.
     * @throws RemoteRetrievalException if an exception occurs during the download process.
     * @throws NoDocumentException if there was no document within the repository identified with
     * the document id.
     */
    byte[] readContents(Document remote) throws RemoteRetrievalException, NoDocumentException;

    /**
     * Executes a search within the remote repository to locate from the set of ids.
     * @param documentSet The query to execute the search.
     * @return An iterable of all documents which matched that search.
     * @throws RemoteRetrievalException if an exception occurs during the search process.
     */
    Iterable<Document> search(Set<Document> documentSet) throws RemoteRetrievalException;
}
