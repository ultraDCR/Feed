package com.example.feed.model;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Date;

public class Post {
    public String _id;
    public String title;
    public String content;
    public String imageUrl;
    public Date createdAt;
    public JSONObject creator;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public JSONObject getCreator() {
        return creator;
    }

    public void setCreator(JSONObject creator) {
        this.creator = creator;
    }
}
