package com.edutechnologic.industrialbadger.content.remote.azure;

import com.edutechnologic.industrialbadger.content.remote.RemoteRepositoryAuthentication;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by H.D. "Chip" McCullough on 4/9/2019.
 */
class AzureBlobStorageAuthentication implements RemoteRepositoryAuthentication {

    private final WeakReference<CloudBlobClient> mClientRef;
    private final String mClientRoot;

    AzureBlobStorageAuthentication(@Nullable CloudBlobClient client, @NonNull String root) {
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
    CloudBlobClient getCloudBlobClient() {
        return mClientRef.get();
    }

    @NonNull
    CloudBlobContainer getRootContainer() throws URISyntaxException, StorageException {
        return getCloudBlobClient().getContainerReference(mClientRoot);
    }
}
