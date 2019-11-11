package com.edutechnologic.industrialbadger.content.api.body;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by H.D. "Chip" McCullough on 12/18/2018.
 */
public class ApplicationToken {
    @Expose
    @SerializedName("ApplicationToken")
    private String value;

    public ApplicationToken(String token) {
        value = token;
    }

    public void setValue(String token) {
        value = token;
    }

    public String getValue() {
        return value;
    }
}
