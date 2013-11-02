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
import com.twitter.university.android.yamba.YambaApplication;
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
        }

        Exception e = null;
        String token = null;
        while (true) {
            try { token = AccountManager.get(ctxt).blockingGetAuthToken(account, AccountMgr.AUTH_TYPE_CLIENT, false); }
            catch (OperationCanceledException oce) { e = oce; }
            catch (AuthenticatorException ae) { e = ae; }
            catch (IOException ioe) { e = ioe; }

            Log.d(TAG, "Got token: " + token);
            if (null == token) {
                Log.e(TAG, "auth failed for: " + AccountMgr.acctStr(account), e);
                return;
            }

            if (null == ((YambaApplication) ctxt.getApplicationContext()).getClientByToken(token)) {
                AccountManager.get(ctxt).invalidateAuthToken(account.type, token);
                continue;
            }

            break;
        }
        YambaService.sync(ctxt, token);
    }
}
