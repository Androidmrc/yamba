package com.twitter.university.android.yamba;

import android.accounts.Account;
import android.app.Application;
import android.util.Log;

import com.twitter.university.android.yamba.sync.AccountMgr;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class YambaApplication extends Application {
    private static final String TAG = "APP";

    private static final Map<Account, String> accounts = new HashMap<Account, String>();

    private static final Map<String, YambaClient> clients = new HashMap<String, YambaClient>();


    public YambaClient getClientByToken(String token) { return clients.get(token); }

    public String createClient(Account account, String handle, String password, String endpoint) {
        Log.d(TAG, "create client: " + AccountMgr.acctStr(account) + "=" + handle + "," + password + "@" + endpoint);
        String token = accounts.get(account);
        if (null == token) {
            token = UUID.randomUUID().toString();
            accounts.put(account, token);
            clients.put(token, new YambaClient(handle, password, endpoint));
        }
        return token;
    }
}
