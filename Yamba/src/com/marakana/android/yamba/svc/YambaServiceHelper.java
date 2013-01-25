package com.marakana.android.yamba.svc;

import android.content.Context;
import android.content.Intent;


public class YambaServiceHelper {
    private static final YambaServiceHelper instance = new YambaServiceHelper();

    public static YambaServiceHelper getInstance() { return instance; }

    public void startPolling(Context ctxt) {
        Intent intent = new Intent(YambaContract.YAMBA_SERVICE);
        intent.putExtra(YambaContract.SVC_PARAM_OP, YambaContract.SVC_OP_POLLING_ON);
        ctxt.startService(intent);
    }

    public void stopPolling(Context ctxt) {
        Intent intent = new Intent(YambaContract.YAMBA_SERVICE);
        intent.putExtra(YambaContract.SVC_PARAM_OP, YambaContract.SVC_OP_POLLING_OFF);
        ctxt.startService(intent);
    }
}
