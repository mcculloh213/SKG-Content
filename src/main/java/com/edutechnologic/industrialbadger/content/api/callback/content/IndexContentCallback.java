package com.edutechnologic.industrialbadger.content.api.callback.content;

import com.edutechnologic.industrialbadger.content.api.model.ContentModel;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by H.D. "Chip" McCullough on 12/18/2018.
 */
public interface IndexContentCallback extends Callback<List<ContentModel>> {
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
    void onResponse(Call<List<ContentModel>> call, Response<List<ContentModel>> response);

    /**
     * Invoked when a network exception occurred talking to the server or when an unexpected
     * exception occurred creating the request or processing the response.
     *
     * @param call
     * @param t
     */
    @Override
    @ParametersAreNonnullByDefault
    void onFailure(Call<List<ContentModel>> call, Throwable t);
}
