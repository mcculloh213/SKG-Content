package com.edutechnologic.industrialbadger.content.service.receiver;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import com.edutechnologic.industrialbadger.base.listener.ActivityProgressBarListener;

import androidx.annotation.NonNull;

import ktx.sovereign.database.entity.Document;

/**
 * Created by H.D. "Chip" McCullough on 4/19/2019.
 */
public class AzureCloudStorageReceiver extends ResultReceiver {
    private static final String TAG = AzureCloudStorageReceiver.class.getSimpleName();

    public static final int RESULT_READY = 1;
    public static final int RESULT_PROGRESS_UPDATE = 2;
    public static final int RESULT_FINISHED = 3;
    public static final int RESULT_ERROR = 255;

    public static final String ARG_REMOTE_DOCUMENT = "com.industrialbadger.azure#ARG_REMOTE_DOCUMENT";
    public static final String ARG_PROGRESS = "com.industrialbadger.azure#ARG_PROGRESS";
    public static final String ARG_ERROR = "com.industrialbadger.imr.content#ARG_ERROR";

    private volatile Callback mReceiver;
    private boolean mKeepAlive = false;


    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public AzureCloudStorageReceiver(Handler handler) {
        super(handler);
    }

    public void registerReceiver(@NonNull Context context, boolean keepAlive) {
        // Log.d(TAG, "registerReceiver");
        if (context instanceof Callback)
            mReceiver = (Callback)context;
        else
            throw new RuntimeException(context.toString()
                    + " must implement AzureCloudStorageReceiver.ICallback");
        mKeepAlive = keepAlive;
    }

    public void unregisterReceiver() {
        mReceiver = null;
    }

    public boolean hasRegisteredReceiver() {
        // Log.d(TAG, "hasRegisteredReceiver");
        return mReceiver != null;
    }

    public void sendReady(Document document) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_REMOTE_DOCUMENT, document);
        send(RESULT_READY, args);
    }

    public void sendProgressUpdate(double progress) {
        Bundle args = new Bundle();
        args.putDouble(ARG_PROGRESS, progress);
        send(RESULT_PROGRESS_UPDATE, args);
    }

    public void sendFinished() {
        send(RESULT_FINISHED, null);
    }

    public void sendError(Exception ex) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ERROR, ex);
        send(RESULT_ERROR, args);
    }

    /**
     * Override to receive results delivered to this object.
     *
     * @param result Arbitrary result code delivered by the sender, as
     *                   defined by the sender.
     * @param resultData Any additional data provided by the sender.
     */
    @Override
    protected void onReceiveResult(final int result, Bundle resultData) {
        switch (result) {
            case RESULT_READY:
                onDownloadReady((Document)resultData.getParcelable(ARG_REMOTE_DOCUMENT));
                break;
            case RESULT_PROGRESS_UPDATE:
                onProgressUpdate(resultData.getDouble(ARG_PROGRESS, 999.99));
                break;
            case RESULT_FINISHED:
                onFinished();
                break;
            case RESULT_ERROR:
                onError(resultData);
                break;
            default:
                break;
        }

        if (!mKeepAlive) mReceiver = null;
    }

    private void onDownloadReady(Document document) {
        String message = "Opening %s from %s";
        if (mReceiver != null)
            mReceiver.showProgressBar(String.format(message, document.getFilename(), document.getRepository()));
    }

    private void onProgressUpdate(double progress) {
        if (mReceiver != null)
            mReceiver.updateProgress(progress);
    }

    private void onFinished() {
        if (mReceiver != null)
            mReceiver.hideProgressBar();
    }

    private void onError(Bundle results) {
        // Log.d(TAG, "onError");
        if (mReceiver != null) {
            mReceiver.hideProgressBar();
            mReceiver.onError((Exception) results.getSerializable(ARG_ERROR));
        }
    }

    public interface Callback extends ActivityProgressBarListener {
        void onError(Exception ex);
    }
}
