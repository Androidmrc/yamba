package com.marakana.android.yamba.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 *
 * @version $Revision: $
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctxt, Intent i) {
        YambaSvc.startPolling(ctxt);
    }
}
