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
import com.marakana.android.yamba.YambaApp;
import com.marakana.android.yamba.YambaContract;
import com.marakana.android.yamba.clientlib.YambaClient.Status;
import com.marakana.android.yamba.clientlib.YambaClientException;


public class YambaSvc extends IntentService {
    private static final String TAG = "SVC";
    private static final long POLL_INTERVAL = 1 * 60 * 1000;
    private static final int MAX_POLL = 50;
    private static final int POLL_REQ = 47;

    private static final String PARAM_STATUS = "YambaSvc.STATUS";
    private static final String PARAM_OP = "YambaSvc.OP";
    private static final int OP_POLL = -1;
    private static final int OP_POST = -2;
    private static final int OP_POST_COMPLETE = -3;

    private static class Hdlr extends Handler {
        private final  YambaSvc svc;

        public Hdlr(YambaSvc svc) { this.svc = svc; }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OP_POST_COMPLETE:
                    svc.postToast(msg.arg1);
                    break;
            }
        }
    }

    public static void post(Context ctxt, String status) {
        Intent i = new Intent(ctxt, YambaSvc.class);
        i.putExtra(PARAM_OP, OP_POST);
        i.putExtra(PARAM_STATUS, status);
        ctxt.startService(i);
    }

    public static void startPolling(Context ctxt) {
        Log.d(TAG, "start poller");
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .setRepeating(
                AlarmManager.RTC,
                System.currentTimeMillis() + 100,
                POLL_INTERVAL,
                createPollIntent(ctxt));
    }

    public static void stopPolling(Context ctxt) {
        Log.d(TAG, "stop poller");
        ((AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE))
            .cancel( createPollIntent(ctxt));
    }

    private static PendingIntent createPollIntent(Context ctxt) {
        Intent i = new Intent(ctxt, YambaSvc.class);
        i.putExtra(PARAM_OP, OP_POLL);
        return PendingIntent.getService(
                ctxt,
                POLL_REQ,
                i,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Handler hdlr;

    public YambaSvc() { super(TAG); }

    @Override
    public void onCreate() {
        super.onCreate();
        hdlr = new Hdlr(this);
    }

    @Override
    protected void onHandleIntent(Intent i) {
        int op = i.getIntExtra(PARAM_OP, 0);
        switch (op) {
            case OP_POLL:
                poll();
                break;
            case OP_POST:
                post(i.getStringExtra(PARAM_STATUS));
                break;
            default:
                Log.w(TAG, "Unrecognized op code: " + op);
        }
    }

    private void poll() {
        List<Status> timeline = null;
        try {
            timeline = ((YambaApp) getApplication()).getClient().getTimeline(MAX_POLL);
        }
        catch (YambaClientException e) {
            Log.w(TAG, "Poll failed", e);
        }
        if (null != timeline) { processTimeline(timeline); }
    }

    private void post(String status) {
        int succ = R.string.fail;
        try {
            ((YambaApp) getApplication()).getClient().postStatus(status);
            succ = R.string.succeed;
        }
        catch (YambaClientException e) {
            Log.w(TAG, "post failed", e);
        }

        Message.obtain(hdlr, OP_POST_COMPLETE, succ, 0).sendToTarget();
    }

    private void processTimeline(List<Status> timeline) {
        List<ContentValues> statuses = new ArrayList<ContentValues>(timeline.size());

        long mostRecentPost = getMostRecentPost();
        Log.d(TAG, "processing after: " + mostRecentPost);

        for (Status status: timeline) {
            long t = status.getCreatedAt().getTime();
            if (t > mostRecentPost) {
                ContentValues vals = new ContentValues();
                vals.put(YambaContract.Timeline.Columns.ID, Long.valueOf(status.getId()));
                vals.put(YambaContract.Timeline.Columns.TIMESTAMP, Long.valueOf(t));
                vals.put(YambaContract.Timeline.Columns.USER,status.getUser());
                vals.put(YambaContract.Timeline.Columns.STATUS, status.getMessage());

                statuses.add(vals);
            }
        }

        if (0 < statuses.size()) {
            getContentResolver().bulkInsert(
                    YambaContract.Timeline.URI,
                    statuses.toArray(new ContentValues[statuses.size()]));
        }
    }

    private long getMostRecentPost() {
        Cursor c =  getContentResolver().query(
                YambaContract.Timeline.URI,
                new String[] { YambaContract.Timeline.Columns.MAX_TIMESTAMP },
                null,
                null,
                null);
        return (c.moveToNext()) ? c.getLong(0) : Long.MIN_VALUE;
    }

    void postToast(int succ) {
        Toast.makeText(this, succ, Toast.LENGTH_LONG).show();
    }
}
