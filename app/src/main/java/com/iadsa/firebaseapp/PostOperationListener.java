package com.iadsa.firebaseapp;

public interface PostOperationListener {

    public void delete(int position, String postId);
    public void edit(int position, Post nPost);
}
