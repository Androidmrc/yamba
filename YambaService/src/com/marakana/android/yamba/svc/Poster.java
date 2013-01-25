package com.marakana.android.yamba.svc;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.marakana.android.yamba.clientlib.YambaClientException;
import com.marakana.android.yamba.svc.YambaService.Op;
import com.marakana.android.yamba.svc.data.ServiceContract;


public class Poster {
    private static final String TAG = "POST";

    private static final String XACT = "Poster.XACT";
    private static final String STATUS = "Poster.STATUS";


    private final YambaApplication ctxt;

    public static void postStatus(Context ctxt, String xact, String status) {
        Intent intent = new Intent(ctxt, YambaService.class);
        intent.putExtra(YambaContract.SVC_PARAM_OP, Op.POST.getCode());
        intent.putExtra(XACT, xact);
        intent.putExtra(STATUS, status);
        ctxt.startService(intent);
    }

    Poster(YambaApplication ctxt) { this.ctxt = ctxt; }

    void postStatus(Bundle args) {
        ContentValues vals = new ContentValues();
        try {
            ctxt.getClient().postStatus(args.getString(STATUS));
            vals.put(ServiceContract.Columns.TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
        }
        catch (YambaClientException e) {
            Log.w(TAG, "post failed: ", e);
        }
        finally {
            cleanupPost(args, vals);
        }
    }

    private void cleanupPost(Bundle args, ContentValues vals) {
        if (null == vals) { vals = new ContentValues(); }
        vals.putNull(ServiceContract.Columns.TRANSACTION);
        ctxt.getContentResolver().update(
                ServiceContract.URI,
                vals,
                ServiceContract.XACT_CONSTRAINT,
                new String[] { args.getString(XACT) });
    }
}
