package com.edutechnologic.industrialbadger.content.remote.azure;

import android.util.Log;

import com.edutechnologic.industrialbadger.base.provider.RemoteDocumentProvider;
import com.edutechnologic.industrialbadger.content.remote.RemoteRepository;
import com.edutechnologic.industrialbadger.content.remote.RemoteRepositoryAuthentication;
import com.edutechnologic.industrialbadger.content.remote.exception.NoDocumentException;
import com.edutechnologic.industrialbadger.content.remote.exception.RemoteRetrievalException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ktx.sovereign.database.entity.Document;

/**
 * Created by H.D. "Chip" McCullough on 4/9/2019.
 */
public class AzureBlobStorage implements RemoteRepository {
    private static final String TAG = AzureBlobStorage.class.getSimpleName();
    private static final String AZURE_STORAGE_CONNECTION_STRING_FORMATTABLE = "" +
            "DefaultEndpointsProtocol=https;" +
            "AccountName=%s;" +
            "AccountKey=%s;" +
            "EndpointSuffix=core.windows.net";

    private final AzureBlobStorageAuthentication mAuthenticator;

    public static AzureBlobStorage newInstance(@NonNull String account, @NonNull String key, @NonNull String container) {
        return new AzureBlobStorage(account, key, container);
    }

    private AzureBlobStorage(@NonNull String account, @NonNull String key, @NonNull String container) {
        mAuthenticator = new AzureBlobStorageAuthentication(authenticate(account, key), container);
    }

    /**
     * Retrieves the authentication provider for this remote repository.
     *
     * @return The remote repository authenticator.
     */
    @Override
    public RemoteRepositoryAuthentication getAuthenticator() {
        return mAuthenticator;
    }

    /**
     * Retrieves the enum value for files stored within this remote repository.
     *
     * @return The remote repository source.
     */
    @Override
    public RemoteDocumentProvider.RemoteSource getSource() {
        return RemoteDocumentProvider.RemoteSource.AZURE_BLOB;
    }

    /**
     * Executes a search within the remote repository with the given query or document ids.
     *
     * @param query The query to execute the search.
     * @return An iterable of all documents which matched that search.
     * @throws RemoteRetrievalException if an exception occurs during the search process.
     */
    @Override
    public Iterable<Document> search(@NonNull String query) throws RemoteRetrievalException {
        return null;
    }

    /**
     * Retrieves the contents of a file as a byte array.
     *
     * @param remote the document id which represents a remote file.
     * @return The contents of the file.
     * @throws RemoteRetrievalException if an exception occurs during the download process.
     * @throws NoDocumentException      if there was no document within the repository identified with
     *                                  the document id.
     */
    @Override
    public byte[] readContents(Document remote) throws RemoteRetrievalException, NoDocumentException {
        if (!mAuthenticator.isAuthenticated()) return new byte[0];

        try {
            CloudBlockBlob blob = mAuthenticator.getRootContainer()
                    .getBlockBlobReference(remote.getIdentifier());

            if (!blob.exists())
                throw new NoDocumentException(
                        "No blob at '" + remote.getIdentifier() + "' exists."
                );

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            blob.download(stream);

            return stream.toByteArray();
        } catch (StorageException e) {
            throw new RemoteRetrievalException(e);
        } catch (URISyntaxException e) {
            throw new RemoteRetrievalException(e);
        }
    }

    /**
     * Executes a search within the remote repository to locate from the set of ids.
     *
     * @param documentSet The query to execute the search.
     * @return An iterable of all documents which matched that search.
     * @throws RemoteRetrievalException if an exception occurs during the search process.
     */
    @Override
    public Iterable<Document> search(Set<Document> documentSet) throws RemoteRetrievalException {
        return null;
    }

    @Nullable
    private CloudBlobClient authenticate(@NonNull String account, @NonNull String key) {
        CloudBlobClient client = null;
        try {
            CloudStorageAccount azure = CloudStorageAccount.parse(
                    String.format(AZURE_STORAGE_CONNECTION_STRING_FORMATTABLE,account, key)
            );
            client = azure.createCloudBlobClient();
        } catch (InvalidKeyException e) {
            Log.e(TAG, e.getMessage());
        } catch (URISyntaxException e) {
           Log.e(TAG, e.getMessage());
        }
        return client;
    }
}
