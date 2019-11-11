package com.edutechnologic.industrialbadger.content.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.edutechnologic.industrialbadger.base.config.AppStorage;
import com.edutechnologic.industrialbadger.base.service.ThreadService;
import com.edutechnologic.industrialbadger.content.api.ContentApi;
import com.edutechnologic.industrialbadger.content.api.callback.content.IndexContentCallback;
import com.edutechnologic.industrialbadger.content.api.model.ContentModel;
import com.edutechnologic.industrialbadger.content.service.receiver.IntelligentMixedRealityContentReceiver;
import com.industrialbadger.api.IntelligentMixedReality;
import com.industrialbadger.api.model.imr.Content;
import com.industrialbadger.api.task.IndexContentTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import ktx.sovereign.database.ApplicationDatabase;
import ktx.sovereign.database.repository.ContentRepository;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by H.D. "Chip" McCullough on 12/18/2018.
 */
public class IntelligentMixedRealityService extends ThreadService implements
        IndexContentCallback {
    private static final String TAG = IntelligentMixedRealityService.class.getSimpleName();

    public static final String ACTION_INITIALIZE = "com.industrialbadger.service.imr.INITIALIZE";
    public static final String ACTION_INDEX_CONTENT = "com.industrialbadger.service.imr.INDEX_CONTENT";

    public static final String EXTRA_TOKEN = "com.industrialbadger.imr.content#EXTRA_TOKEN";
    public static final String EXTRA_URI = "com.industrialbadger.imr.content#EXTRA_URI";
    private String mToken;
    private String mUri;

    private static IntelligentMixedRealityContentReceiver mReceiver;
    private ContentRepository mContentRepository;
    private ContentApi mApi;
    private Looper mReceiverLooper;

    public static void startActionInitialize(@NonNull Context context,
                                             @NonNull String url, @NonNull String token) {
        // Log.d(TAG, "startActionInitialize");
        Intent intent = new Intent(context, IntelligentMixedRealityService.class);
        intent.setAction(ACTION_INITIALIZE);
        intent.putExtra(EXTRA_URI, url);
        intent.putExtra(EXTRA_TOKEN, token);
        context.startService(intent);
    }

    public static void startActionIndex(@NonNull Context context, boolean keepAlive) {
        // Log.d(TAG, "startActionIndex");
        if (!mReceiver.hasRegisteredReceiver()) registerReceiver(context, keepAlive);
        Intent intent = new Intent(context, IntelligentMixedRealityService.class);
        intent.setAction(ACTION_INDEX_CONTENT);
        context.startService(intent);
    }

    public IntelligentMixedRealityService() {
        super(TAG);
        setIntentRedelivery(true);
    }

    /**
     * Called after {@link #onCreate()} has been called and the initial service thread has been
     * set up.
     */
    @Override
    protected void onPostCreate() {
        // Log.d(TAG, "onPostCreate");
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
        // Log.d(TAG, "onHandleIntent");
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_INITIALIZE:
                    handleActionInitialize(intent.getStringExtra(EXTRA_URI), intent.getStringExtra(EXTRA_TOKEN));
                    break;
                case ACTION_INDEX_CONTENT:
                    handleActionIndexContent();
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
        // Log.d(TAG, "onDestroy");
        mReceiverLooper.quit();
        super.onDestroy();
    }
    //endregion

    private void initializeServiceLoopers() {
        // Log.d(TAG, "initializeServiceLoopers");
        HandlerThread thread = new HandlerThread(
                String.format("ResultReceiver[%s]", IntelligentMixedRealityContentReceiver.class.getSimpleName())
        );
        thread.start();

        mReceiverLooper = thread.getLooper();
        mReceiver = new IntelligentMixedRealityContentReceiver(new Handler(mReceiverLooper));
    }

    private static void registerReceiver(@NonNull Context context, boolean keepAlive) {
        // Log.d(TAG, "registerReceiver");
        mReceiver.registerReceiver(context, keepAlive);
    }

    private void handleActionInitialize(String url, String token) {
        // Log.d(TAG, "handleActionInitialize");
        mApi = ContentApi.getInstance(url);
        mToken = token;
        mContentRepository = new ContentRepository(ApplicationDatabase.getDatabase(this).ContentDao());
    }

    private void handleActionIndexContent() {
        // Log.d(TAG, "handleActionIndexContent");
//        new IndexContentTask(IntelligentMixedRealityService.this, mApi, mToken).execute();
        IntelligentMixedReality.getInstance().indexContent(IntelligentMixedRealityService.this,
                new IndexContentTask.Companion.Callback() {
                    @Override
                    public void updateProgress(int current, int count) {
                        Bundle args = new Bundle();
                        args.putInt(IntelligentMixedRealityContentReceiver.ARG_PROGRESS_VALUE, current);
                        args.putInt(IntelligentMixedRealityContentReceiver.ARG_PROGRESS_COUNT, count);
                        if (mReceiver != null)
                            mReceiver.send(IntelligentMixedRealityContentReceiver.RESULT_PROGRESS, args);
                    }

                    @Override
                    public String exists(@NonNull String id) {
                        ktx.sovereign.database.entity.Content content = mContentRepository.getAsync(id);
                        return content == null ? null : content.getChecksum();
                    }

                    @Override
                    public void writeToDatabase(@NotNull Content.Content content) {
//                        ktx.sovereign.database.entity.Content entity =
//                                new ktx.sovereign.database.entity.Content(
//                                        content.getId(),
//                                        content.getFilename(),
//                                        content.getOwner(),
//                                        content.getApplication()
//                                                .concat(File.separator)
//                                                .concat(content.getFilename()),
//                                        content.getApplication(),
//                                        content.getChecksum()
//                                );
//                        mContentRepository.insertAsync(entity); //.addContent(entity, new ContentRepositoryOnCompletionListener());
                    }

                    @Override
                    public void writeToFile(@NonNull String application, @NonNull String filename, @NotNull String content) {
                        File file = AppStorage.MakeContentFile(
                                IntelligentMixedRealityService.this,
                                AppStorage.EXTERNAL_CONTENT_DIR,
                                application,
                                filename
                        );
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                            writer.write(content);
                            writer.flush();
                        } catch (IOException ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                    }

                    @Override
                    public void complete(@NotNull List<Content.Content> content) {
                        if (mReceiver != null)
                            mReceiver.send(IntelligentMixedRealityContentReceiver.RESULT_INDEXED, null);
                    }
                });
    }

    //region IndexContentCallback
    /**
     * Invoked for a received HTTP response.
     * <p>
     * Note: An HTTP response may still indicate an application-level failure such as a 404 or 500.
     * Call {@link Response#isSuccessful()} to determine if the response indicates success.
     *
     * @param call
     * @param response
     */
    @Override
    @ParametersAreNonnullByDefault
    public void onResponse(Call<List<ContentModel>> call, Response<List<ContentModel>> response) {
        // Log.d(TAG, "onResponse");
    }

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call
     * @param t
     */
    @Override
    @ParametersAreNonnullByDefault
    public void onFailure(Call<List<ContentModel>> call, Throwable t) {
        Bundle args = new Bundle();
        args.putSerializable(IntelligentMixedRealityContentReceiver.ARG_ERROR, t);
        if (mReceiver != null)
            mReceiver.send(IntelligentMixedRealityContentReceiver.RESULT_ERROR, args);

    }
    //endregion

//    @Override
//    public void onReadyForAnalysis(@NonNull Content content) {
//        Log.d(TAG, "onReadyForAnalysis");
//        TextMiningService.startActionRake(IntelligentMixedRealityService.this, content);
//    }
//
//    //region IndexContentTask.OnTaskCompletedListener
//    @Override
//    public void onProgress(Integer value, Integer count) {
//        // Log.d(TAG, "updateProgress");
//        Bundle args = new Bundle();
//        args.putInt(IntelligentMixedRealityContentReceiver.ARG_PROGRESS_VALUE, value);
//        args.putInt(IntelligentMixedRealityContentReceiver.ARG_PROGRESS_COUNT, count);
//        if (mReceiver != null)
//            mReceiver.send(IntelligentMixedRealityContentReceiver.RESULT_PROGRESS, args);
//    }

//    @Override
//    public void onSuccess(List<Content> content) {
//        // Log.d(TAG, "onSuccess");
//        if (mContentRepository != null)
//            mContentRepository.addContent(content, new ContentRepositoryOnCompletionListener());
//        if (mReceiver != null)
//            mReceiver.send(IntelligentMixedRealityContentReceiver.RESULT_INDEXED, null);
//    }
//
//    @Override
//    public void onCancelled(List<Content> content) {
//        // Log.d(TAG, "onCancelled");
//    }
//
//    @Override
//    public void onFailure(ResponseBody body) {
//        // Log.d(TAG, "onFailure");
//    }
    //endregion

//    private class ContentRepositoryOnCompletionListener implements
//            DeleteContentTask.OnCompletionListener,
//            InsertContentTask.OnCompletionListener,
//            UpdateContentTask.OnCompletionListener {
//
//        @Override
//        public void onDeleteResults(Integer count) {
//
//        }
//
//        @Override
//        public void onInsertComplete(List<Long> ids) {
//
//        }
//
//        @Override
//        public void onUpdateResults(Integer count) {
//
//        }
//    }
}
