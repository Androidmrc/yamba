package com.twitter.university.android.yamba.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.twitter.university.android.yamba.BuildConfig;


public class SyncService extends Service {
    private static final String TAG = "SYNC_SVC";


    private volatile SyncAdapter synchronizer;

    @Override
    public void onCreate() {
        super.onCreate();
        synchronizer = new SyncAdapter(getApplication(), true);
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "sync bound"); }
        return synchronizer.getSyncAdapterBinder();
    }
}
