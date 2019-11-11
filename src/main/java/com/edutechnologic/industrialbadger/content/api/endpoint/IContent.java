package com.edutechnologic.industrialbadger.content.api.endpoint;

import com.edutechnologic.industrialbadger.content.api.body.ApplicationToken;
import com.edutechnologic.industrialbadger.content.api.model.ContentModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by H.D. "Chip" McCullough on 12/17/2018.
 */
public interface IContent {
    @POST("/Texts")
    Call<List<ContentModel>> index(@Body ApplicationToken token);
}
