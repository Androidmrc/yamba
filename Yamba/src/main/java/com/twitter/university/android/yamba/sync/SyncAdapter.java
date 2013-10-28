package com.twitter.university.android.yamba.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.twitter.university.android.yamba.BuildConfig;
import com.twitter.university.android.yamba.R;
import com.twitter.university.android.yamba.service.YambaService;

import java.io.IOException;


public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SYNC";

    public static void dump(String tag, Bundle bundle) {
        for (String key: bundle.keySet()) {
            Log.d(tag, key + " = " + bundle.get(key));
        }
    }


    private final Context ctxt;

    public SyncAdapter(Context ctxt, boolean autoInitialize) {
        super(ctxt, autoInitialize);
        this.ctxt = ctxt;
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle extras,
            String authority,
            ContentProviderClient provider,
            SyncResult syncResult)
    {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "starting sync: " + AccountMgr.acctStr(account) + "@" + authority);
            dump(TAG, extras);
        }

        AccountManager mgr = AccountManager.get(ctxt);

        String tt = ctxt.getString(R.string.token_type);

        Exception e = null;
        String token = null;
        try { token = mgr.blockingGetAuthToken(account, tt, false); }
        catch (OperationCanceledException oce) { e = oce; }
        catch (AuthenticatorException ae) { e = ae; }
        catch (IOException ioe) { e = ioe; }

        if (null == token) {
            Log.e(TAG, "auth failed: " + AccountMgr.acctStr(account) + "#" + tt, e);
            return;
        }


        // !!! make this work
        YambaService.sync(ctxt, account.name, token, null);

        // ??? force re-validation
        mgr.invalidateAuthToken(account.type, token);
    }
}
