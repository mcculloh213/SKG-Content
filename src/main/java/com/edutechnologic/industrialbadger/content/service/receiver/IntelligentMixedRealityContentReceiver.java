package com.edutechnologic.industrialbadger.content.service.receiver;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Created by H.D. "Chip" McCullough on 3/27/2019.
 */
public class IntelligentMixedRealityContentReceiver extends ResultReceiver {
    private static final String TAG = IntelligentMixedRealityContentReceiver.class.getSimpleName();

    public static final int RESULT_INITIALIZED = 1;
    public static final int RESULT_PROGRESS = 2;
    public static final int RESULT_INDEXED = 4;
    public static final int RESULT_ERROR = 255;

    public static final String ARG_PROGRESS_VALUE = "com.industrialbadger.imr.content#ARG_PROGRESS_VALUE";
    public static final String ARG_PROGRESS_COUNT = "com.industrialbadger.imr.content#ARG_PROGRESS_COUNT";
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
    public IntelligentMixedRealityContentReceiver(Handler handler) {
        super(handler);
    }

    public void registerReceiver(@NonNull Context context, boolean keepAlive) {
        // Log.d(TAG, "registerReceiver");
        if (context instanceof Callback)
            mReceiver = (Callback)context;
        else
            throw new RuntimeException(context.toString()
                    + " must implement IntelligentMixedRealityContentReceiver.ICallback");
        mKeepAlive = keepAlive;
    }

    public boolean hasRegisteredReceiver() {
        // Log.d(TAG, "hasRegisteredReceiver");
        return mReceiver != null;
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
            case RESULT_INITIALIZED:
                onInitialized();
                break;
            case RESULT_PROGRESS:
                onProgress(resultData);
                break;
            case RESULT_INDEXED:
                onIndexed(resultData);
                break;
            case RESULT_ERROR:
                onError(resultData);
                break;
            default:
                break;
        }

        if (!mKeepAlive) mReceiver = null;
    }

    private void onInitialized() {
        // Log.d(TAG, "onInitialized");
    }

    private void onProgress(Bundle results) {
        // Log.d(TAG, "updateProgress");
        if (mReceiver != null)
            mReceiver.onContentProgress(results.getInt(ARG_PROGRESS_VALUE, 0),
                    results.getInt(ARG_PROGRESS_COUNT, 0));
    }

    private void onIndexed(Bundle results) {
        // Log.d(TAG, "onIndexed");
        if (mReceiver != null)
            mReceiver.onContentIndexed();
    }

    private void onError(Bundle results) {
        // Log.d(TAG, "onError");
        if (mReceiver != null)
            mReceiver.onError((Exception)results.getSerializable(ARG_ERROR));
    }

    public interface Callback {
        void onContentProgress(int value, int count);
        void onContentIndexed();
        void onError(Exception ex);
    }
}
