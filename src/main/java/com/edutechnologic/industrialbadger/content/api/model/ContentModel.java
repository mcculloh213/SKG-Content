package com.edutechnologic.industrialbadger.content.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by H.D. "Chip" McCullough on 12/17/2018.
 */
public class ContentModel implements Serializable {

    @SerializedName("ID")
    @Expose
    private int id;

    @SerializedName("Title")
    @Expose
    private String title;

    @SerializedName("Description")
    @Expose
    private String description;

    public ContentModel() {

    }

    public ContentModel(int id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
