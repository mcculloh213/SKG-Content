package com.edutechnologic.industrialbadger.content.service;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.edutechnologic.industrialbadger.base.provider.RemoteDocumentProvider;
import com.edutechnologic.industrialbadger.base.service.ThreadService;
import com.edutechnologic.industrialbadger.base.util.MimeType;
import com.edutechnologic.industrialbadger.content.PortableDocumentFormatViewerActivity;
import com.edutechnologic.industrialbadger.content.remote.azure.AzureBlobStorage;
import com.edutechnologic.industrialbadger.content.remote.azure.AzureFileStorage;
import com.edutechnologic.industrialbadger.content.remote.exception.NoDocumentException;
import com.edutechnologic.industrialbadger.content.remote.exception.RemoteRetrievalException;
import com.edutechnologic.industrialbadger.content.service.receiver.AzureCloudStorageReceiver;
import com.microsoft.azure.storage.StorageException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ktx.sovereign.database.entity.Document;

/**
 * Created by H.D. "Chip" McCullough on 4/11/2019.
 */
public class AzureCloudStorageService extends ThreadService implements
        AzureFileStorage.DownloadStatusListener {
    private static final String TAG = AzureCloudStorageService.class.getSimpleName();

    public static final String ACTION_INDEX = "com.industrialbadger.azure.INDEX";
    public static final String ACTION_CREATE = "com.industrialbadger.azure.CREATE";
    public static final String ACTION_READ = "com.industrialbadger.azure.READ";
    public static final String ACTION_UPDATE = "com.industrialbadger.azure.UPDATE";
    public static final String ACTION_DELETE = "com.industrialbadger.azure.DELETE";

    public static final String EXTRA_ACCOUNT = "com.industrialbadger.azure#EXTRA_ACCOUNT";
    public static final String EXTRA_KEY = "com.industrialbadger.azure#EXTRA_KEY";
    public static final String EXTRA_ROOT = "com.industrialbadger.azure#EXTRA_ROOT";

    public static final String EXTRA_FLAT_DIR = "com.industrialbadger.azure#EXTRA_FLAT_DIR";
    public static final String EXTRA_REMOTE_DOCUMENT = "com.industrialbadger.azure#EXTRA_REMOTE_DOCUMENT";

    private AzureBlobStorage mAzureBlobStorage;
    private AzureFileStorage mAzureFileStorage;

    private static AzureCloudStorageReceiver mReceiver;
    private static Looper mReceiverLooper;

    public static void registerReceiver(@NonNull Context context, boolean keepAlive) {
        if (mReceiver == null) {
            if (mReceiverLooper == null) {
                HandlerThread thread = new HandlerThread(
                        String.format("ResultReceiver[%s]", AzureCloudStorageService.class.getSimpleName())
                );
                thread.start();
                mReceiverLooper = thread.getLooper();
            }
            mReceiver = new AzureCloudStorageReceiver(new Handler(mReceiverLooper));
        }
        mReceiver.registerReceiver(context, keepAlive);
    }

    public static void unregisterReceiver() {
        if (mReceiver != null)
            mReceiver.unregisterReceiver();
    }

    /**
     *
     * @param context
     * @param account
     * @param key
     * @param company
     */
    public static void startIndex(@NonNull Context context,
                                  @NonNull String account, @NonNull String key, @NonNull String company) {
        Intent intent = new Intent(context, AzureCloudStorageService.class);
        intent.setAction(ACTION_INDEX);
        intent.putExtra(EXTRA_ACCOUNT, account);
        intent.putExtra(EXTRA_KEY, key);
        intent.putExtra(EXTRA_ROOT, company);
        context.startService(intent);
    }

    public static void readRemoteDocument(@NonNull Context context, @NonNull Document document,
                                          @NonNull String account, @NonNull String key, @NonNull String company) {
        Intent intent = new Intent(context, AzureCloudStorageService.class);
        intent.setAction(ACTION_READ);
        intent.putExtra(EXTRA_REMOTE_DOCUMENT, document);
        intent.putExtra(EXTRA_ACCOUNT, account);
        intent.putExtra(EXTRA_KEY, key);
        intent.putExtra(EXTRA_ROOT, company);
        context.startService(intent);
    }

    private static void openFile(@NonNull Context context, @NonNull File file, @NonNull String fileName, @NonNull String extension) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(
                Uri.fromFile(file),
//                RemoteDocumentProvider.getUriForFile(context, RemoteDocumentProvider.AUTHORITY, file),
                MimeType.getMimeType(extension));
        intent.putExtra(PortableDocumentFormatViewerActivity.ARG_FILENAME, fileName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
            Toast.makeText(context.getApplicationContext(),
                    String.format("Could not open %s", file.getName()), Toast.LENGTH_LONG).show();
        }
    }

    public AzureCloudStorageService() {
        super(TAG);
    }

    /**
     * Called after {@link #onCreate()} has been called and the initial service thread has been
     * set up.
     */
    @Override
    protected void onPostCreate() {
        super.onPostCreate();
        initializeServiceLoopers();
    }

    /**
     * This method is invoked on the worker thread with a request to process.
     * Only one Intent is processed at a time, but the processing happens on a
     * worker thread that runs independently from other application logic.
     * So, if this code takes a long time, it will hold up other requests to
     * the same IntentService, but it will not hold up anything else.
     * When all requests have been handled, the IntentService stops itself,
     * so you should not call {@link #stopSelf}.
     *
     * @param intent The value passed to {@link
     *               Context#startService(Intent)}.
     *               This may be null if the service is being restarted after
     *               its process has gone away; see
     *               {@link Service#onStartCommand}
     *               for details.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_INDEX:
                    onIndexFileStorage(
                            intent.getStringExtra(EXTRA_ACCOUNT),
                            intent.getStringExtra(EXTRA_KEY),
                            intent.getStringExtra(EXTRA_ROOT)
                    );
                    break;
                case ACTION_CREATE:
                    break;
                case ACTION_READ:
                    onReadFile(
                            (Document)intent.getParcelableExtra(EXTRA_REMOTE_DOCUMENT),
                            intent.getStringExtra(EXTRA_ACCOUNT),
                            intent.getStringExtra(EXTRA_KEY),
                            intent.getStringExtra(EXTRA_ROOT)
                    );
                    break;
                case ACTION_UPDATE:
                    break;
                case ACTION_DELETE:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        mReceiverLooper.quit();
        super.onDestroy();
    }

    private void initializeServiceLoopers() {
        HandlerThread thread = new HandlerThread(
                String.format("ResultReceiver[%s]", AzureCloudStorageService.class.getSimpleName())
        );
        thread.start();
        mReceiverLooper = thread.getLooper();
        mReceiver = new AzureCloudStorageReceiver(new Handler(mReceiverLooper));
    }

    private void onIndexFileStorage(@NonNull String account, @NonNull String key, @NonNull String share) {
//        AzureFileStorage storage = createFileStorage(account, key, share);
//        RemoteDocumentRepository repo = new RemoteDocumentRepository(getApplication());
//        if (storage != null) {
//            try {
//                repo.insert(storage.indexDepthFirst(storage.getRootDirectory(), 0));
//            } catch (URISyntaxException e) {
//                Log.e(TAG, e.getMessage());
//            } catch (StorageException e) {
//                Log.e(TAG, e.getMessage());
//            }
//        }
    }

    private void onReadFile(@NonNull Document document, @NonNull String account, @NonNull String key, @NonNull String share) {
        // Is the document a valid "readable" (i.e. not a directory)?
        if (document.getMime().equals("directory")) return;
        AzureFileStorage storage = createFileStorage(account, key, share);

        try {
            byte[] data = storage.readContents(document, this);
            File out = RemoteDocumentProvider.cacheRemoteDocument(AzureCloudStorageService.this, document, data);
            AzureCloudStorageService.openFile(AzureCloudStorageService.this, out, document.getFilename(), document.getMime());
        } catch (RemoteRetrievalException e) {
            Log.e(TAG, e.getMessage());
            if (mReceiver.hasRegisteredReceiver())
                mReceiver.sendError(e);
        } catch (NoDocumentException e) {
            Log.e(TAG, e.getMessage());
            if (mReceiver.hasRegisteredReceiver())
                mReceiver.sendError(e);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            if (mReceiver.hasRegisteredReceiver())
                mReceiver.sendError(e);
        }

        // Is the document stored locally, or is it being read remotely?
    }

    private AzureBlobStorage createBlobStorage(@NonNull String account, @NonNull String key, @NonNull String container) {
        return AzureBlobStorage.newInstance(account, key, container);
    }

    private AzureFileStorage createFileStorage(@NonNull String account, @NonNull String key, @NonNull String share) {
        return AzureFileStorage.newInstance(account, key, share);
    }

    private double toPercent(long remaining, long size) {
        return (((double)size - (double)remaining) / (double)size) * 100.00;
    }

    //region AzureFileStorage.DownloadStatusListener
    @Override
    public void onReady(Document target) {
        if (mReceiver.hasRegisteredReceiver())
            mReceiver.sendReady(target);
    }

    @Override
    public void onProgress(long blockSize, long remaining, long size) {
//        // Log.d(TAG, String.format("File Size: %s -- Block Size: %s -- Remaining: %s -- %s",
//                size, blockSize, remaining, toPercent(remaining, size)));
        if (mReceiver.hasRegisteredReceiver())
            mReceiver.sendProgressUpdate(toPercent(remaining, size));
    }

    @Override
    public void onFinished() {
        if (mReceiver.hasRegisteredReceiver())
            mReceiver.sendFinished();
    }
    //endregion
}
