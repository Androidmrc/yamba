package com.marakana.android.yamba.svc;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClientException;
import com.marakana.android.yamba.svc.YambaService.Op;
import com.marakana.android.yamba.svc.data.ServiceContract;


public class Poller {
    private static final String TAG = "POLL";

    public static final long POLL_INTERVAL = 2 * 60 * 1000;

    private static final int INTENT_TAG = 42;

    private static Intent getPollIntent(Context ctxt) {
        Intent intent = new Intent(ctxt, YambaService.class);
        intent.putExtra(YambaContract.SVC_PARAM_OP, Op.POLL.getCode());
        return intent;
    }

	public static void startPolling(Context ctxt) {
		((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .setRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis() + 100,
                POLL_INTERVAL,
                PendingIntent.getService(
                    ctxt,
                    INTENT_TAG,
                    getPollIntent(ctxt),
                    PendingIntent.FLAG_UPDATE_CURRENT));
        if (BuildConfig.DEBUG) { Log.d(TAG, "Polling started"); }
	}

    public static void stopPolling(Context ctxt) {
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
        .cancel(
            PendingIntent.getService(
                    ctxt,
                    INTENT_TAG,
                    getPollIntent(ctxt),
                    PendingIntent.FLAG_UPDATE_CURRENT));
        if (BuildConfig.DEBUG) { Log.d(TAG, "Polling stopped"); }
    }


    private final YambaApplication ctxt;

    Poller(YambaApplication ctxt) { this.ctxt = ctxt; }

    void pollStatus() {
        if (BuildConfig.DEBUG) { Log.d(TAG, "Fetching status updates"); }

        List<YambaClient.Status> statuses = null;
        Exception fail = null;
        try { statuses = ctxt.getClient().getTimeline(); }
        catch (NullPointerException e) { fail = e; }
        catch (YambaClientException e) { fail = e; }

        if (null != fail) {
            Log.e(TAG, "Failed to fetch status updates", fail);
            return;
        }

        addAll(statuses);
    }

    private int addAll(List<YambaClient.Status> statuses) {
        long mostRecentStatus = getLatestStatusCreatedAtTime();
        List<ContentValues> update = new ArrayList<ContentValues>(statuses.size());
        for (YambaClient.Status status: statuses) {
            long t = status.getCreatedAt().getTime();
            if (mostRecentStatus <= t) {
                ContentValues vals = new ContentValues();
                vals.put(ServiceContract.Columns.ID, Long.valueOf(status.getId()));
                vals.put(ServiceContract.Columns.TIMESTAMP, Long.valueOf(t));
                vals.put(ServiceContract.Columns.USER, status.getUser());
                vals.put(ServiceContract.Columns.STATUS, status.getMessage());
                update.add(vals);
            }
        }

        int added = 0;
        if (0 < update.size()) {
            added = ctxt.getContentResolver().bulkInsert(
                    ServiceContract.URI,
                    update.toArray(new ContentValues[update.size()]));
        }

        return added;
    }

    private long getLatestStatusCreatedAtTime() {
        Cursor c = ctxt.getContentResolver().query(
                ServiceContract.URI,
                new String[] { ServiceContract.Columns.MAX_TIMESTAMP },
                null,
                null,
                null);
        try { return (c.moveToNext()) ? c.getLong(0) : Long.MIN_VALUE; }
        finally { c.close(); }
    }
}
