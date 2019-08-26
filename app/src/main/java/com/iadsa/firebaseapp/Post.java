package com.iadsa.firebaseapp;

import android.net.Uri;

public class Post {
    public static final String POST_ID = "postId";
    public static final String TITLE = "title";
    public static final String BODY = "body";
    private String postId;
    private String title;
    private String body;
    private Uri url;

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Uri getUrl() {
        return url;
    }

    public void setUrl(Uri url) {
        this.url = url;
    }
}
