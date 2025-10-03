package com.example.rileymest;

import android.app.Application;

import com.example.rileymest.data.remote.MongoHelper;

public class MyApp extends Application {
    private MongoHelper mongo;

    @Override
    public void onCreate() {
        super.onCreate();
        mongo = MongoHelper.get();
        mongo.connect();
        mongo.connectBlocking();
    }

    public MongoHelper getMongo() {
        return mongo;
    }
}