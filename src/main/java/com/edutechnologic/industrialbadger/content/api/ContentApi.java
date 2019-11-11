package com.edutechnologic.industrialbadger.content.api;

import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.edutechnologic.industrialbadger.content.api.body.ApplicationToken;
import com.edutechnologic.industrialbadger.content.api.callback.content.IndexContentCallback;
import com.edutechnologic.industrialbadger.content.api.endpoint.IContent;
import com.edutechnologic.industrialbadger.content.api.model.ContentModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by H.D. "Chip" McCullough on 12/18/2018.
 */
public class ContentApi {
    private static final String TAG = ContentApi.class.getSimpleName();

    private IContent mContentRoutes;

    private static ContentApi sInstance;
    private static final HashMap<String, ApplicationToken> sTokenCache = new HashMap<>();

    public static ContentApi getInstance(@NonNull final String baseUrl) {
        // Log.d(TAG, "getInstance");

        if (sInstance == null) {
            synchronized (ContentApi.class) {
                if (sInstance == null) {
                    sInstance = new ContentApi(baseUrl);
                }
            }
        }

        return sInstance;
    }

    //region Private Constructor
    /**
     * Private Constructor.
     *
     * @param baseUrl The base URL for the API.
     */
    private ContentApi(@NonNull final String baseUrl) {
        Retrofit builder = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mContentRoutes = builder.create(IContent.class);
    }
    //endregion

    //region API (Async)
    /**
     * Fetches a list of all available content files.
     *
     * @param token The IMR Account Token.
     * @param callback The async {@link IndexContentCallback} handler.
     */
    public void index(@NonNull String token, @NonNull IndexContentCallback callback) {
        // Log.d(TAG, "index");

        Call<List<ContentModel>> req = mContentRoutes.index(getToken(token));
        req.enqueue(callback);
    }
    //endregion

    //region API (Synchronous)
    /**
     * <b>Synchronously</b> "Creates" a new account.
     * <p>
     *     Since this is a synchronous method, it should not be called from the main thread.
     *     The thread that made the call will be blocked while executing. If this method is called
     *     from the main thread, it will throw a {@link NetworkOnMainThreadException}.
     * </p>
     *
     * @param token The IMR Account Token.
     * @throws IOException Thrown if the request encounters a network exception.
     * @throws NetworkOnMainThreadException Thrown if this method is called from the Main Thread.
     */
    public Response<List<ContentModel>> index(@NonNull String token) throws
            IOException, NetworkOnMainThreadException {
        // Log.d(TAG, "index");

        return mContentRoutes.index(getToken(token)).execute();
    }
    //endregion

    private ApplicationToken getToken(@NonNull String token) {
        // Log.d(TAG, "getToken");

        ApplicationToken applicationToken = sTokenCache.get(token);
        if (applicationToken == null) {
            applicationToken = new ApplicationToken(token);
            sTokenCache.put(token, applicationToken);
        }

        return applicationToken;
    }
}
