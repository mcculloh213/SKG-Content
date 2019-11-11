package com.edutechnologic.industrialbadger.content.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.edutechnologic.industrialbadger.base.config.AppStorage;
import com.edutechnologic.industrialbadger.base.listener.ActivityProgressBarListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;

import ktx.sovereign.database.entity.Content;

/**
 * Created by H.D. "Chip" McCullough on 3/12/2019.
 */
public class ReadFileTask extends AsyncTask<Content, Void, String> {
    private static final String TAG = ReadFileTask.class.getSimpleName();

    private final WeakReference<Context> mContextRef;
    private OnCompletionListener mListener;

    public ReadFileTask(@NonNull Context context, OnCompletionListener listener) {
        mContextRef = new WeakReference<>(context);
        mListener = listener;
    }

    /**
     * Runs on the UI thread before {@link #doInBackground}.
     *
     * @see #onPostExecute
     * @see #doInBackground
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.showLoadingDialog();
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param content The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected String doInBackground(Content... content) {
        if (content.length != 1)
            throw new RuntimeException("Invalid number of arguments: " + content.length + ".");
        return readFile(getContentFile(content[0]));
    }

    /**
     * Runs on the UI thread after {@link #publishProgress} is invoked.
     * The specified values are the values passed to {@link #publishProgress}.
     *
     * @param values The values indicating progress.
     * @see #publishProgress
     * @see #doInBackground
     */
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }


    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     *
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param s The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mListener.onReadResults(s);
    }
    /**
     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground(Object[])} has finished.</p>
     *
     * <p>The default implementation simply invokes {@link #onCancelled()} and
     * ignores the result. If you write your own implementation, do not call
     * <code>super.onCancelled(result)</code>.</p>
     *
     * @param s The result, if any, computed in
     *          {@link #doInBackground(Object[])}, can be null
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
        mListener.hideLoadingDialog();
    }

    /**
     * <p>Applications should preferably override {@link #onCancelled(Object)}.
     * This method is invoked by the default implementation of
     * {@link #onCancelled(Object)}.</p>
     *
     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground(Object[])} has finished.</p>
     *
     * @see #onCancelled(Object)
     * @see #cancel(boolean)
     * @see #isCancelled()
     */
    @Override
    protected void onCancelled() {
        super.onCancelled();
        mListener.hideLoadingDialog();
    }

    private Context getContext() {
        return mContextRef.get();
    }

    private File getContentFile(Content content) {
        // Log.d(TAG, "getContentFile");
        return AppStorage.GetContentFile(
                getContext(), content.getToken(), content.getTitle()
        );
    }

    private String readFile(File file) {
        // Log.d(TAG, "readFile");
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append('\n');
        } catch(FileNotFoundException ex) {
            Log.e(TAG, ex.getMessage());
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }

        return sb.toString();
    }

    public interface OnCompletionListener extends ActivityProgressBarListener {
        void onReadResults(String contents);
    }
}
