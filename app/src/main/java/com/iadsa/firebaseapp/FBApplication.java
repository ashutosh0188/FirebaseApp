package com.iadsa.firebaseapp;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class FBApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
