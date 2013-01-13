package com.marakana.android.yamba.svc;

import java.util.UUID;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.marakana.android.yamba.BuildConfig;
import com.marakana.android.yamba.YambaApplication;


public class YambaService extends IntentService {
    private static final String TAG = "SVC";

    static final String OP = "YambaService.OP";
    public static enum Op {
        NOOP, POLL, POST;

        static Op fromCode(int code) {
            Op[] ops = Op.values();
            code = (code * -1) - 1;
            return ((0 > code) || (ops.length <= code))
                ? NOOP
                : ops[code];
        }

        int getCode() { return (ordinal() + 1) * -1; }
    }


    public static String getTransactionId() { return UUID.randomUUID().toString(); }


    public YambaService() { super(TAG); }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle args = intent.getExtras();
        int op = args.getInt(OP);
        if (BuildConfig.DEBUG) { Log.d(TAG, "handle op: " + op); }

        switch (Op.fromCode(op)) {
            case POST:
                new Poster((YambaApplication) getApplication()).postStatus(args);
                break;

            case POLL:
                new Poller((YambaApplication) getApplication()).pollStatus();
                break;

            default:
                throw new IllegalArgumentException("Unrecognized op: " + op);
        }
    }
}