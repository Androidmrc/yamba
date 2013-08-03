package com.marakana.android.yamba.svc;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.marakana.android.yamba.R;
import com.marakana.android.yamba.YambaApplication;
import com.marakana.android.yamba.YambaContract;
import com.marakana.android.yamba.clientlib.YambaClient.Status;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    static final int OP_POST_COMPLETE = -1;
    private static final int OP_POST = -2;
    private static final int OP_POLL = -3;

    private static final String PARAM_OP = "YambaService.OP";
    private static final String PARAM_STATUS = "YambaService.STATUS";

    private static final int POLL_REQ = 42;

    private static class Hdlr extends Handler {
        private final  YambaService svc;

        public Hdlr(YambaService svc) { this.svc = svc; }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case OP_POST_COMPLETE:
                    Toast.makeText(svc, msg.arg1, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    public static void startPolling(Context ctxt) {
        Log.d(TAG, "Polling started");
        long t = 1000 * 60 * ctxt.getResources().getInteger(R.integer.poll_interval);
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .setInexactRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis() + 100,
                t,
                getPollingIntent(ctxt));
    }

    public static void stopPolling(Context ctxt) {
        Log.d(TAG, "Polling stopped");
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .cancel(getPollingIntent(ctxt));
    }

    public static void post(Context ctxt, String msg) {
        Intent i = new Intent(ctxt, YambaService.class);
        i.putExtra(PARAM_OP, OP_POST);
        i.putExtra(PARAM_STATUS, msg);
        ctxt.startService(i);
    }

    private static PendingIntent getPollingIntent(Context ctxt) {
        Intent i = new Intent(ctxt, YambaService.class);
        i.putExtra(PARAM_OP, OP_POLL);
        return PendingIntent.getService(
                ctxt,
                POLL_REQ,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private volatile int maxPolls;
    private volatile Hdlr hdlr;

    public YambaService() { super(TAG); }

    @Override
    public void onCreate() {
        super.onCreate();
        hdlr = new Hdlr(this);
        maxPolls = getResources().getInteger(R.integer.poll_max);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int op = intent.getIntExtra(PARAM_OP, 0);
        switch(op) {
            case OP_POST:
                doPost(intent);
                break;
            case OP_POLL:
                doPoll();
                break;

            default:
                throw new IllegalArgumentException("Unrecognized op: " + op);
        }
    }

    private void doPoll() {
        Log.d(TAG, "poll");
        try {
            parseTimeline(
                ((YambaApplication) getApplication()).getClient().getTimeline(maxPolls));
        }
        catch (Exception e) {
            Log.e(TAG, "Poll failed: " + e, e);
        }
    }

    private void doPost(Intent intent) {
        String status = intent.getStringExtra(PARAM_STATUS);
        Log.d(TAG, "post: " + status);

        int msg = R.string.statusFailed;
        try {
            ((YambaApplication) getApplication()).getClient().postStatus(status);
            msg = R.string.statusSucceeded;
        }
        catch (Exception e) {
            Log.e(TAG, "Post failed: " + e, e);
        }

        Message.obtain(hdlr, OP_POST_COMPLETE, msg, 0).sendToTarget();
    }

    private void parseTimeline(List<Status> timeline) {
        long latest = getMaxTimestamp();
        List<ContentValues> vals = new ArrayList<ContentValues>();
        for (Status status: timeline) {
            long t = status.getCreatedAt().getTime();
            if (t <= latest) { continue; }

            ContentValues row = new ContentValues();
            row.put(YambaContract.Timeline.Column.ID, Long.valueOf(status.getId()));
            row.put(YambaContract.Timeline.Column.TIMESTAMP, Long.valueOf(t));
            row.put(YambaContract.Timeline.Column.USER, status.getUser());
            row.put(YambaContract.Timeline.Column.STATUS, status.getMessage());

            Log.d(TAG, "Insert: " + row);
            vals.add(row);
        }

        getContentResolver().bulkInsert(
                YambaContract.Timeline.URI,
                vals.toArray(new ContentValues[vals.size()]));
    }

    private long getMaxTimestamp() {
        Cursor c = null;
        long mx = Long.MIN_VALUE;
        try {
            c = getContentResolver().query(
                    YambaContract.Timeline.URI,
                    new String[] { YambaContract.Timeline.Column.MAX_TIMESTAMP },
                    null,
                    null,
                    null);
            if (c.moveToNext()) { mx = c.getLong(0); }
        }
        finally {
            if (null != c) { c.close(); }
        }
        return mx;
    }
}
