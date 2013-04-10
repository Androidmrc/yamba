package com.marakana.android.yamba.svc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.marakana.android.yamba.BuildConfig;
import com.marakana.android.yamba.YambaApplication;
import com.marakana.android.yamba.YambaContract;
import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClientException;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    public static final long POLL_INTERVAL = 2 * 60 * 1000;
    public static final int MAX_POSTS = 60;

    private static final String SVC_PARAM_OP = "YambaService.OP";
    private static final int OP_START_POLLING = 6001;
    private static final int OP_STOP_POLLING = 6002;
    private static final int OP_POLL = 6003;
    private static final int OP_POST = 6004;

    private static final String SVC_PARAM_STATUS = "YambaService.STATUS";
    private static final String SVC_PARAM_XACT = "YambaService.XACT";

    private static final int INTENT_TAG = 42;

    // simplified version
    public static void post(Context ctxt, String status) { post(ctxt, null, status); }

    public static void post(Context ctxt, ContentValues vals, String status) {
        Intent intent = new Intent(ctxt, YambaService.class);
        intent.putExtra(SVC_PARAM_OP, OP_POST);
        intent.putExtra(SVC_PARAM_STATUS, status);

        if (null != vals) {
            String xactId = UUID.randomUUID().toString();
            vals.put(YambaContract.Posts.Columns.TRANSACTION, xactId);
            intent.putExtra(SVC_PARAM_XACT, xactId);
        }

        ctxt.startService(intent);
    }

    public static void startPolling(Context ctxt) {
        Intent intent = new Intent(ctxt, YambaService.class);
        intent.putExtra(SVC_PARAM_OP, OP_START_POLLING);
        ctxt.startService(intent);
    }

    public static void stopPolling(Context ctxt) {
        Intent intent = new Intent(ctxt, YambaService.class);
        intent.putExtra(SVC_PARAM_OP, OP_STOP_POLLING);
        ctxt.startService(intent);
    }


    public YambaService() { super(TAG); }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle args = intent.getExtras();

        int op = args.getInt(SVC_PARAM_OP);
        if (BuildConfig.DEBUG) { Log.d(TAG, "handle op: " + op); }

        switch (op) {
            case OP_POST:
                postStatus(args);
                break;

            case OP_START_POLLING:
                startTimelinePolling();
                break;

            case OP_STOP_POLLING:
                stopTimelinePolling();
                break;

            case OP_POLL:
                pollTimeline();
                break;

            default:
                throw new IllegalArgumentException("Unrecognized op: " + op);
        }
    }

    void postStatus(Bundle args) {
        ContentValues vals = new ContentValues();
        try {
            String status = args.getString(SVC_PARAM_STATUS);
            ((YambaApplication) getApplication()).getClient().postStatus(status);
            if (BuildConfig.DEBUG) { Log.d(TAG, "Posted: " + status); }
            vals.put(YambaContract.Posts.Columns.TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
        }
        catch (YambaClientException e) {
            Log.w(TAG, "post failed: ", e);
        }
        finally {
            cleanupPost(args, vals);
        }
    }

    private void startTimelinePolling() {
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
        .setRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis() + 100,
                POLL_INTERVAL,
                PendingIntent.getService(
                        this,
                        INTENT_TAG,
                        getPollIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT));
        if (BuildConfig.DEBUG) { Log.d(TAG, "Polling started"); }
    }

    private void stopTimelinePolling() {
        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
        .cancel(
                PendingIntent.getService(
                        this,
                        INTENT_TAG,
                        getPollIntent(),
                        PendingIntent.FLAG_UPDATE_CURRENT));
        if (BuildConfig.DEBUG) { Log.d(TAG, "Polling stopped"); }
    }


    private void pollTimeline() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "Fetching timeline"); }

        List<YambaClient.Status> statuses = null;
        Exception fail = null;
        try { statuses = ((YambaApplication) getApplication()).getClient().getTimeline(MAX_POSTS); }
        catch (NullPointerException e) { fail = e; }
        catch (YambaClientException e) { fail = e; }

        if (null != fail) {
            Log.e(TAG, "Failed to fetch status updates", fail);
            return;
        }

        int n = addAll(statuses);
        if (BuildConfig.DEBUG) { Log.d(TAG, n + " records added to timeline"); }
    }

    private int addAll(List<YambaClient.Status> statuses) {
        long mostRecentStatus = getLatestStatusCreatedAtTime();
        List<ContentValues> update = new ArrayList<ContentValues>(statuses.size());
        for (YambaClient.Status status: statuses) {
            long t = status.getCreatedAt().getTime();
            if (t > mostRecentStatus) {
                ContentValues vals = new ContentValues();
                vals.put(YambaContract.Timeline.Columns.ID, Long.valueOf(status.getId()));
                vals.put(YambaContract.Timeline.Columns.TIMESTAMP, Long.valueOf(t));
                vals.put(YambaContract.Timeline.Columns.USER, status.getUser());
                vals.put(YambaContract.Timeline.Columns.STATUS, status.getMessage());
                update.add(vals);
            }
        }

        int added = 0;
        if (0 < update.size()) {
            added = getContentResolver().bulkInsert(
                    YambaContract.Timeline.URI,
                    update.toArray(new ContentValues[update.size()]));
        }

        return added;
    }

    private long getLatestStatusCreatedAtTime() {
        Cursor c = getContentResolver().query(
                YambaContract.Timeline.URI,
                new String[] { YambaContract.Timeline.Columns.MAX_TIMESTAMP },
                null,
                null,
                null);
        try { return (c.moveToNext()) ? c.getLong(0) : Long.MIN_VALUE; }
        finally { c.close(); }
    }

    private void cleanupPost(Bundle args, ContentValues vals) {
        if (null == vals) { vals = new ContentValues(); }
        vals.putNull(YambaContract.Posts.Columns.TRANSACTION);
        getContentResolver().update(
                YambaContract.Posts.URI,
                vals,
                YambaContract.Posts.XACT_CONSTRAINT,
                new String[] { args.getString(SVC_PARAM_XACT) });
    }

    private Intent getPollIntent() {
        Intent intent = new Intent(this, YambaService.class);
        intent.putExtra(SVC_PARAM_OP, OP_POLL);
        return intent;
    }
}