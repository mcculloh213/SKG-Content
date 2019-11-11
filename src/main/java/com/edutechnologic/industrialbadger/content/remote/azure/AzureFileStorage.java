package com.edutechnologic.industrialbadger.content.remote.azure;

import android.util.Log;

import com.edutechnologic.industrialbadger.base.provider.RemoteDocumentProvider;
import com.edutechnologic.industrialbadger.content.remote.RemoteRepository;
import com.edutechnologic.industrialbadger.content.remote.RemoteRepositoryAuthentication;
import com.edutechnologic.industrialbadger.content.remote.exception.NoDocumentException;
import com.edutechnologic.industrialbadger.content.remote.exception.RemoteRetrievalException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFile;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.ListFileItem;

import java.io.ByteArrayOutputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ktx.sovereign.database.entity.Document;

/**
 * Created by H.D. "Chip" McCullough on 4/11/2019.
 */
public class AzureFileStorage implements RemoteRepository {
    private static final String TAG = AzureFileStorage.class.getSimpleName();
    private static final String AZURE_STORAGE_CONNECTION_STRING_FORMATTABLE = "" +
            "DefaultEndpointsProtocol=https;" +
            "AccountName=%s;" +
            "AccountKey=%s;" +
            "EndpointSuffix=core.windows.net";
    private static final long MEGABYTE_BLOCK = 1024 * 1024; // 1 MB Chunk

    private final AzureFileStorageAuthentication mAuthenticator;

    public static AzureFileStorage newInstance(@NonNull String account, @NonNull String key, @NonNull String container) {
        return new AzureFileStorage(account, key, container);
    }

    private AzureFileStorage(@NonNull String account, @NonNull String key, @NonNull String container) {
        mAuthenticator = new AzureFileStorageAuthentication(authenticate(account, key), container);
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

    public CloudFileDirectory getRootDirectory() throws URISyntaxException, StorageException {
        return mAuthenticator.getRootDirectory();
    }

    /**
     * Retrieves the enum value for files stored within this remote repository.
     *
     * @return The remote repository source.
     */
    @Override
    public RemoteDocumentProvider.RemoteSource getSource() {
        return RemoteDocumentProvider.RemoteSource.AZURE_FILE;
    }

    public List<Document> indexDepthFirst(@NonNull CloudFileDirectory directory, int depth) throws URISyntaxException, StorageException {
        List<Document> documents = new ArrayList<>();
        for (ListFileItem item : directory.listFilesAndDirectories()) {
            if (item instanceof CloudFileDirectory) {
                CloudFileDirectory dir = (CloudFileDirectory)item;
                // Log.d(TAG, String.format("Directory: %s", dir.getName()));
//                documents.add(new Document(
//                        depth == 0 ? Document.RootDirectory.getRemoteId() : directory.getUri().getPath(),
//                        getSource().getName(),
//                        dir.getUri().getPath(),
//                        dir.getName(),
//                        "directory",
//                        depth
//                ));
                documents.addAll(indexDepthFirst(dir, depth+1));
            } else {
                CloudFile file = item.getParent().getFileReference(((CloudFile)item).getName());
                String name = file.getName();
//                documents.add(new Document(
//                        directory.getUri().getPath(),
//                        getSource().getName(),
//                        file.getUri().getPath(),
//                        name,
//                        name.substring(name.lastIndexOf('.')),
//                        depth
//                ));
            }
        }

        return documents;
    }

    public List<Document> indexBreadthFirst(@NonNull CloudFileDirectory directory, @Nullable Queue<CloudFileDirectory> queue, int depth) {
        List<Document> documents = new ArrayList<>();
        if (queue == null)
            queue = new LinkedList<>();

        for (ListFileItem item : directory.listFilesAndDirectories()) {
            if (item instanceof CloudFileDirectory) {
                CloudFileDirectory dir = (CloudFileDirectory)item;
                queue.add(dir);
//                documents.add(new RemoteDocument(
//                        dir.getUri().getPath(),
//                        getSource().getName(),
//                        dir.getUri().getPath(),
//                        dir.getName(),
//                        "directory",
//                        depth
//                ));
            } else {
                CloudFile file = (CloudFile)item;
                String name = file.getName();
//                try {
//                    documents.add(new RemoteDocument(
//                            directory.getParent().getUri().getPath(),
//                            getSource().getName(),
//                            file.getUri().getPath(),
//                            name,
//                            name.substring(name.lastIndexOf('.')),
//                            depth
//                    ));
//                } catch (URISyntaxException e) {
//                    Log.e(TAG, e.getMessage());
//                    e.printStackTrace();
//                } catch (StorageException e) {
//                    Log.e(TAG, e.getMessage());
//                    e.printStackTrace();
//                }
            }
        }

        if (!queue.isEmpty()) {
            documents.addAll(indexBreadthFirst(queue.remove(), queue, depth+1));
        }

        return documents;
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
            CloudFile file = mAuthenticator
                    .getSubDirectory(mAuthenticator.getRootDirectory(), remote.getIdentifier())
                    .getFileReference(remote.getFilename());

            if (!file.exists())
                throw new NoDocumentException(
                        "No file at '" + remote.getIdentifier() + "' exists."
                );

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            file.download(stream);

            return stream.toByteArray();
        } catch (StorageException e) {
            throw new RemoteRetrievalException(e);
        } catch (URISyntaxException e) {
            throw new RemoteRetrievalException(e);
        }
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
    public byte[] readContents(Document remote, DownloadStatusListener listener) throws RemoteRetrievalException, NoDocumentException {
        if (!mAuthenticator.isAuthenticated()) return new byte[0];

        try {
            CloudFile file = mAuthenticator
                    .getSubDirectory(mAuthenticator.getRootDirectory(), remote.getIdentifier())
                    .getFileReference(remote.getFilename());

            if (!file.exists())
                throw new NoDocumentException(
                        "No file at '" + remote.getIdentifier() + "' exists."
                );
            // Log.d(TAG, String.format("Write Size: %s -- Minimum Read Size: %s", file.getStreamWriteSizeInBytes(), file.getStreamMinimumReadSizeInBytes()));
            listener.onReady(remote);
            long fileSize = file.getProperties().getLength(); // File Size (Bytes)
            long remaining = file.getProperties().getLength();
            long ptr = 0;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            do {
                listener.onProgress(MEGABYTE_BLOCK, remaining, fileSize);
                file.downloadRange(ptr, MEGABYTE_BLOCK, stream);
                ptr += MEGABYTE_BLOCK;
                remaining -= MEGABYTE_BLOCK;
            } while (remaining > 0);
            listener.onFinished();
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
    private CloudFileClient authenticate(@NonNull String account, @NonNull String key) {
        CloudFileClient client = null;
        try {
            CloudStorageAccount azure = CloudStorageAccount.parse(
                    String.format(AZURE_STORAGE_CONNECTION_STRING_FORMATTABLE,account, key)
            );
            client = azure.createCloudFileClient();
        } catch (InvalidKeyException e) {
            Log.e(TAG, e.getMessage());
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
        }
        return client;
    }

    public interface DownloadStatusListener {
        void onReady(Document target);
        void onProgress(long blockSize, long remaining, long size);
        void onFinished();
    }
}
