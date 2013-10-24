package com.twitter.university.android.yamba.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.twitter.university.android.yamba.BuildConfig;
import com.twitter.university.android.yamba.R;
import com.twitter.university.android.yamba.TimelineActivity;
import com.twitter.university.android.yamba.YambaApplication;
import com.twitter.university.android.yamba.data.YambaContract;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    private static final int POLLER = 666;
    private static final int NOTIFICATION_ID = 7;
    private static final int NOTIFICATION_INTENT_ID = 13;

    private static final String IS_NULL = " is null ";
    private static final String IS_EQ = "=?";

    public static void post(Context ctxt, String tweet) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "post: " + tweet); }
        Intent i = new Intent(ctxt, YambaService.class);
        i.setAction(YambaContract.Service.ACTION_POST);
        i.putExtra(YambaContract.Service.PARAM_TWEET, tweet);
        ctxt.startService(i);
    }

    public static void startPoller(Context ctxt) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "start poller"); }
        Intent i = new Intent(ctxt, YambaService.class);
        i.setAction(YambaContract.Service.ACTION_START_POLLING);
        ctxt.startService(i);
    }

    public static void stopPoller(Context ctxt) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "stop poller"); }
        Intent i = new Intent(ctxt, YambaService.class);
        i.setAction(YambaContract.Service.ACTION_STOP_POLLING);
        ctxt.startService(i);
    }

    private static void postComplete(Context ctxt) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "post complete"); }
        Intent i = new Intent(ctxt, YambaService.class);
        i.setAction(YambaContract.Service.ACTION_POST_COMPLETE);
        ctxt.startService(i);
    }

    private static void sync(Context ctxt) {
        ctxt.startService(createSyncIntent(ctxt));
    }

    private static Intent createSyncIntent(Context ctxt) {
        Intent i = new Intent(ctxt, YambaService.class);
        i.setAction(YambaContract.Service.ACTION_SYNC);
        return i;
    }

    private volatile int pollSize;
    private volatile long pollInterval;

    public YambaService() { super(TAG); }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) { Log.d(TAG, "created"); }

        Resources rez = getResources();
        pollSize = rez.getInteger(R.integer.poll_size);
        pollInterval = rez.getInteger(R.integer.poll_interval) * 60 * 1000;
   }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) { Log.d(TAG, "destroyed"); }
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId) {
        int ret = 0;

        String op = i.getAction();
        if (YambaContract.Service.ACTION_POST_COMPLETE.equals(op)) {
            doPostComplete(i.getStringExtra(YambaContract.Service.PARAM_TWEET));
        }
        else {
            ret = super.onStartCommand(i, flags, startId);
        }

        return ret;
    }

    @Override
    protected void onHandleIntent(Intent i) {
        String op = i.getAction();
        if (BuildConfig.DEBUG) { Log.d(TAG, "exec: " + op); }
        if (YambaContract.Service.ACTION_POST.equals(op)) {
            doPost(i.getStringExtra(YambaContract.Service.PARAM_TWEET));
        }
        else if (YambaContract.Service.ACTION_START_POLLING.equals(op)) {
            doStartPoller();
        }
        else if (YambaContract.Service.ACTION_STOP_POLLING.equals(op)) {
            doStopPoller();
        }
        else if (YambaContract.Service.ACTION_SYNC.equals(op)) {
            doSync();
        }
        else {
            Log.e(TAG, "Unexpected op: " + op);
        }
    }

    private void doPost(String tweet) {
        ContentValues cv = new ContentValues();
        cv.put(YambaContract.Posts.Columns.TWEET, tweet);
        cv.put(YambaContract.Posts.Columns.TIMESTAMP, System.currentTimeMillis());
        getContentResolver().insert(YambaContract.Posts.URI, cv);
        sync(this);
    }

    private void doPostComplete(String tweet) {
        Toast.makeText(this, R.string.tweet_sent, Toast.LENGTH_LONG).show();
    }

    private void doStartPoller() {
        if (0 >= pollInterval) { return; }
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
            .setInexactRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis() + 100,
                pollInterval,
                createPollerIntent());
    }

    private void doStopPoller() {
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
                .cancel(createPollerIntent());
    }

    private void doSync() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "sync"); }

        YambaClient client;
        try { client = getClient(); }
        catch (YambaClientException e) {
            Log.e(TAG, "Failed to get client", e);
            return;
        }

        try { notifyPost(postPending(client)); }
        catch (YambaClientException e) {
            Log.e(TAG, "post failed", e);
        }

        try { notifyTimelineUpdate(parseTimeline(client.getTimeline(pollSize))); }
        catch (YambaClientException e) {
            Log.e(TAG, "poll failed", e);
            e.printStackTrace();
        }
    }

    private int postPending(YambaClient client) throws YambaClientException {
        ContentResolver cr = getContentResolver();

        String xactId = UUID.randomUUID().toString();

        ContentValues row = new ContentValues();
        row.put(YambaContract.Posts.Columns.TRANSACTION, xactId);

        int n = cr.update(
            YambaContract.Posts.URI,
            row,
            YambaContract.Posts.Columns.SENT + IS_NULL
                + "AND " + YambaContract.Posts.Columns.TRANSACTION + IS_NULL,
            null);

        if (BuildConfig.DEBUG) { Log.d(TAG, "pending: " + n); }
        if (0 >= n) { return 0; }

        Cursor cur = null;
        try {
            cur = cr.query(
                YambaContract.Posts.URI,
                null,
                YambaContract.Posts.Columns.TRANSACTION + IS_EQ,
                new String[] { xactId },
                YambaContract.Posts.Columns.TIMESTAMP + " ASC");
            return postTweets(cur, client);
        }
        finally {
            if (null != cur) { cur.close(); }
            row.clear();
            row.putNull(YambaContract.Posts.Columns.TRANSACTION);
            cr.update(
                YambaContract.Posts.URI,
                row,
                YambaContract.Posts.Columns.TRANSACTION + IS_EQ,
                new String[] { xactId });
        }
    }

    private int postTweets(Cursor c, YambaClient client) throws YambaClientException {
        int idIdx = c.getColumnIndex(YambaContract.Posts.Columns.ID);
        int tweetIdx = c.getColumnIndex(YambaContract.Posts.Columns.TWEET);

        int n = 0;
        ContentValues row = new ContentValues();
        while (c.moveToNext()) {
            client.postStatus(c.getString(tweetIdx));
            row.clear();
            row.put(YambaContract.Posts.Columns.SENT, System.currentTimeMillis());
            Uri uri = YambaContract.Posts.URI.buildUpon().appendPath(c.getString(idIdx)).build();
            n += getContentResolver().update(uri, row, null, null);
        }
        return n;
    }

    private int parseTimeline(List<YambaClient.Status> timeline) {
        long latest = getLatestTweetTime();
        if (BuildConfig.DEBUG) { Log.d(TAG, "latest: " + latest); }

        List<ContentValues> vals = new ArrayList<ContentValues>();

        for (YambaClient.Status tweet: timeline) {
            long t = tweet.getCreatedAt().getTime();
            if (t <= latest) { continue; }

            ContentValues cv = new ContentValues();
            cv.put(YambaContract.Timeline.Columns.ID, Long.valueOf(tweet.getId()));
            cv.put(YambaContract.Timeline.Columns.TIMESTAMP, Long.valueOf(t));
            cv.put(YambaContract.Timeline.Columns.HANDLE, tweet.getUser());
            cv.put(YambaContract.Timeline.Columns.TWEET, tweet.getMessage());
            vals.add(cv);
        }

        int n = vals.size();
        if (0 >= n) { return 0; }
        n = getContentResolver().bulkInsert(
            YambaContract.Timeline.URI,
            vals.toArray(new ContentValues[n]));

        if (BuildConfig.DEBUG) { Log.d(TAG, "inserted: " + n); }
        return n;
    }

    private long getLatestTweetTime() {
        Cursor c = null;
        try {
            c = getContentResolver().query(
                YambaContract.Timeline.URI,
                new String[] { "max(" + YambaContract.Timeline.Columns.TIMESTAMP + ")" },
                null,
                null,
                null);
            return ((null == c) || (!c.moveToNext()))
                ? Long.MIN_VALUE
                : c.getLong(0);
        }
        finally {
            if (null != c) { c.close(); }
        }
    }

    private void notifyPost(int count) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "posted: " + count); }
        if (count <= 0) { return; }
        postComplete(this);
    }

    private PendingIntent createPollerIntent() {
        return PendingIntent.getService(
            this,
            POLLER,
            createSyncIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private YambaClient getClient() throws YambaClientException {
        return ((YambaApplication) getApplication()).getYambaClient();
    }

    private void notifyTimelineUpdate(int count) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "timeline: " + count); }
        if (count <= 0) { return; }

        Resources rez = getResources();
        String notifyTitle = rez.getString(R.string.notify_title);
        String notifyMessage = rez.getString(R.string.notify_message);

        if (0 >= count) { return; }

        PendingIntent pi = PendingIntent.getActivity(
            this,
            NOTIFICATION_INTENT_ID,
            new Intent(this, TimelineActivity.class),
            0);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
            .notify(
                NOTIFICATION_ID,
                new Notification.Builder(this)
                    .setContentTitle(notifyTitle)
                    .setContentText(count + " " + notifyMessage)
                    .setAutoCancel(true)
                    .setSmallIcon(android.R.drawable.stat_notify_more)
                    .setWhen(System.currentTimeMillis())
                    .setContentIntent(pi)
                        .build());  // works as of version 16
    }
}

