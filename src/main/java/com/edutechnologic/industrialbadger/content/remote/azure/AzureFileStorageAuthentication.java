package com.edutechnologic.industrialbadger.content.remote.azure;

import com.edutechnologic.industrialbadger.content.remote.RemoteRepositoryAuthentication;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileDirectory;
import com.microsoft.azure.storage.file.CloudFileShare;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by H.D. "Chip" McCullough on 4/11/2019.
 */
class AzureFileStorageAuthentication implements RemoteRepositoryAuthentication {

    private final WeakReference<CloudFileClient> mClientRef;
    private final String mClientRoot;

    AzureFileStorageAuthentication(@Nullable CloudFileClient client, @NonNull String root) {
        mClientRef = new WeakReference<>(client);
        mClientRoot = root;
    }

    /**
     * Authenticates the user with a remote repository.
     *
     * @return true if the user is authenticated, false otherwise.
     */
    @Override
    public boolean isAuthenticated() {
        return mClientRef.get() != null;
    }

    @NonNull
    CloudFileClient getCloudFileClient() {
        return mClientRef.get();
    }

    @NonNull
    CloudFileShare getRootShare() throws URISyntaxException, StorageException {
        return getCloudFileClient().getShareReference(mClientRoot);
    }

    @NonNull
    CloudFileDirectory getRootDirectory() throws URISyntaxException, StorageException {
        return getRootShare().getRootDirectoryReference();
    }

    @NonNull
    CloudFileDirectory getSubDirectory(@NonNull CloudFileDirectory current, @NonNull String path)
            throws URISyntaxException, StorageException {
        CloudFileDirectory directory = current;
        CloudFileDirectory temp;
        if (path.startsWith("/"))
            path = path.substring(1);

        for (String token : path.split("/")) {
            temp = directory.getDirectoryReference(token);
            if (temp.exists())
                directory = temp;
        }

        return directory;
    }
}
