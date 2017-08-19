package com.mwiti.collins.inspector;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import timber.log.Timber;

/**
 * Created by collins on 8/19/17.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        INSTANCE = this;
        client = new ClarifaiBuilder(getString(R.string.clarifai_id), getString(R.string.clarifai_secret))
                // Optionally customize HTTP client via a custom OkHttp instance
                .client(new OkHttpClient.Builder()
                        .readTimeout(30, TimeUnit.SECONDS) // This is to increase timeout for poor mobile networks

                        // Log all incoming and outgoing data
                        .addInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                            @Override public void log(String logString) {
                                Timber.e(logString);
                            }
                        }).setLevel(HttpLoggingInterceptor.Level.BODY))
                        .build()
                )
                .buildSync(); // I have used build() instead to get a Future<Clarifai Client>, if I don't want to block this thread
        super.onCreate();

        // Initialize our logging
        Timber.plant(new Timber.DebugTree());
    }

    // In an app, rather than attaching singletons(a class that can have only one object such as the API client instance) to your Application instance,
    // it's recommended that you use e.g. Dagger 2 (most efficient dependency injection frameworks where it  analyzes these dependencies for you and generates code to help wire them together.), and inject your client instance.
    // Since that would be a distraction here, we will just use a regular singleton.
    private static App INSTANCE;

    @Nullable
    private ClarifaiClient client;

    @NonNull
    public ClarifaiClient clarifaiClient() {
        final ClarifaiClient client = this.client;
        if (client == null) {
            throw new IllegalStateException("You cannot use client before its initialized");
        }
        return client;
    }


    //is used to indicate to the user if the instance is not created, it should notify the user to create the app
    @NonNull
    public static App get() {
        final App instance = INSTANCE;
        if (instance == null) {
            throw new IllegalStateException("Please create your app!");
        }
        return instance;
    }


}
