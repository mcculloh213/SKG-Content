//package com.edutechnologic.industrialbadger.content.service.task;
//
//import android.content.Context;
//import android.os.AsyncTask;
//import android.util.Log;
//
//import com.edutechnologic.industrialbadger.base.config.AppStorage;
//import com.edutechnologic.industrialbadger.content.api.ContentApi;
//import com.edutechnologic.industrialbadger.content.api.model.ContentModel;
//import com.edutechnologic.industrialbadger.database.entities.Content;
//import com.edutechnologic.industrialbadger.database.util.EncodingUtil;
//import com.industrialbadger.api.IntelligentMixedReality;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.lang.ref.WeakReference;
//import java.util.ArrayList;
//import java.util.List;
//
//import androidx.annotation.NonNull;
//import okhttp3.ResponseBody;
//import retrofit2.Response;
//
///**
// * Created by H.D. "Chip" McCullough on 12/18/2018.
// */
//public class IndexContentTask extends AsyncTask<Void, Integer, List<Content>> {
//    private static final String TAG = IndexContentTask.class.getSimpleName();
//
//    private final WeakReference<Context> mContextRef;
//    private final WeakReference<ContentApi> mApiRef;
//    private final WeakReference<String> mTokenRef;
//
//    private final WeakReference<String> mUserIdRef;
//
//    private final OnTaskCompletedListener mListener;
//
//    public IndexContentTask(@NonNull Context context, @NonNull ContentApi api, @NonNull String token) {
//        mContextRef = new WeakReference<>(context);
//        mApiRef = new WeakReference<>(api);
//        mTokenRef = new WeakReference<>(token);
//
//        mUserIdRef = new WeakReference<>("");
//        if (context instanceof OnTaskCompletedListener) {
//            mListener = (OnTaskCompletedListener)context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement IndexContentTask#OnTaskCompletedListener");
//        }
//    }
//
//    /**
//     * Override this method to perform a computation on a background thread. The
//     * specified parameters are the parameters passed to {@link #execute}
//     * by the caller of this task.
//     * <p>
//     * This method can call {@link #publishProgress} to publish updates
//     * on the UI thread.
//     *
//     * @param voids The parameters of the task.
//     * @return A result, defined by the subclass of this task.
//     * @see #onPreExecute()
//     * @see #onPostExecute
//     * @see #publishProgress
//     */
//    @Override
//    protected List<Content> doInBackground(Void... voids) {
//        ArrayList<Content> content = new ArrayList<>();
//        File file;
//        String token = mTokenRef.get();
//        try {
//            Response<List<ContentModel>> response = mApiRef.get().index(token);
//
//            if (response.isSuccessful()) {
//                List<ContentModel> body = response.body();
//                if (body != null) {
//                    final int count = body.size();
//                    int i = 1;
//                    publishProgress(0, count);
//
//                    for (ContentModel res : body) {
//                        file = writeContentToFile(res);
//                        if (file != null) {
//                            Content entity = new Content((long) res.getId(), token, res.getTitle(),
//                                    token.concat(File.separator).concat(file.getName()),
//                                    EncodingUtil.generateMD5Hash(file));
//                            content.add(entity);
//                            mListener.onReadyForAnalysis(entity);
//                        }
//                        publishProgress(i, count);
//                        i++;
//                    }
//                }
//                return content;
//            } else {
//                mListener.onFailure(response.errorBody());
//                return content;
//            }
//        } catch (IOException ex) {
//            Log.e(TAG, ex.getMessage());
//            return content;
//        }
//    }
//
//    /**
//     * Runs on the UI thread after {@link #publishProgress} is invoked.
//     * The specified values are the values passed to {@link #publishProgress}.
//     *
//     * @param values The values indicating progress.
//     * @see #publishProgress
//     * @see #doInBackground
//     */
//    @Override
//    protected void onProgressUpdate(Integer... values) {
//        super.onProgressUpdate(values);
//        if (values.length >= 2) mListener.onProgress(values[0], values[1]);
//    }
//
//    /**
//     * <p>Runs on the UI thread after {@link #doInBackground}. The
//     * specified result is the value returned by {@link #doInBackground}.</p>
//     *
//     * <p>This method won't be invoked if the task was cancelled.</p>
//     *
//     * @param content The result of the operation computed by {@link #doInBackground}.
//     * @see #onPreExecute
//     * @see #doInBackground
//     * @see #onCancelled(Object)
//     */
//    @Override
//    protected void onPostExecute(List<Content> content) {
//        super.onPostExecute(content);
//        mListener.onSuccess(content);
//    }
//
//    /**
//     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
//     * {@link #doInBackground(Object[])} has finished.</p>
//     *
//     * <p>The default implementation simply invokes {@link #onCancelled()} and
//     * ignores the result. If you write your own implementation, do not call
//     * <code>super.onCancelled(result)</code>.</p>
//     *
//     * @param content The result, if any, computed in
//     *                 {@link #doInBackground(Object[])}, can be null
//     * @see #cancel(boolean)
//     * @see #isCancelled()
//     */
//    @Override
//    protected void onCancelled(List<Content> content) {
//        super.onCancelled(content);
//    }
//
//    /**
//     * <p>Applications should preferably override {@link #onCancelled(Object)}.
//     * This method is invoked by the default implementation of
//     * {@link #onCancelled(Object)}.</p>
//     *
//     * <p>Runs on the UI thread after {@link #cancel(boolean)} is invoked and
//     * {@link #doInBackground(Object[])} has finished.</p>
//     *
//     * @see #onCancelled(Object)
//     * @see #cancel(boolean)
//     * @see #isCancelled()
//     */
//    @Override
//    protected void onCancelled() {
//        super.onCancelled();
//    }
//
////    private String writeContentToFile(@NonNull ContentModel content) {
////        // Log.d(TAG, String.format("writeContentToFile: %s", content.getTitle()));
////
////        BufferedWriter writer;
////        File file = AppStorage.MakeContentFile(
////                mContextRef.get(), AppStorage.EXTERNAL_CONTENT_DIR, mTokenRef.get(), content.getTitle()
////        );
////
////        try {
////            writer = new BufferedWriter(new FileWriter(file));
////            writer.write(content.getDescription());
////            writer.flush();
////            writer.close();
////        } catch (IOException ex) {
////            Log.e(TAG, ex.getMessage());
////            return null;
////        }
////
////        return mTokenRef.get().concat(File.separator).concat(file.getName());
////    }
//
//    private File writeContentToFile(@NonNull ContentModel content) {
//        // Log.d(TAG, String.format("writeContentToFile: %s", content.getTitle()));
//
//        BufferedWriter writer;
//        File file = AppStorage.MakeContentFile(
//                mContextRef.get(), AppStorage.EXTERNAL_CONTENT_DIR, mTokenRef.get(), content.getTitle()
//        );
//
//        try {
//            writer = new BufferedWriter(new FileWriter(file));
//            writer.write(content.getDescription());
//            writer.flush();
//            writer.close();
//        } catch (IOException ex) {
//            Log.e(TAG, ex.getMessage());
//            return null;
//        }
//
//        return file;
//    }
//
//    public interface OnTaskCompletedListener {
//        void onReadyForAnalysis(@NonNull Content content);
//
//        void onProgress(Integer value, Integer count);
//
//        void onSuccess(List<Content> content);
//
//        void onCancelled(List<Content> content);
//
//        void onFailure(ResponseBody body);
//    }
//}
