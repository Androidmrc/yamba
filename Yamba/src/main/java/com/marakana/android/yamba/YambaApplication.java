package com.marakana.android.yamba;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.svc.YambaService;


public class YambaApplication extends Application
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "APP";

    private YambaClient client;
    private String userKey;
    private String pwdKey;
    private String uriKey;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Yamba started!");

        userKey = getString(R.string.prefsKeyUser);
        pwdKey = getString(R.string.prefsKeyPass);
        uriKey = getString(R.string.prefsKeyURI);

        // Don't use an anonymous class to handle this event!
        // http://stackoverflow.com/questions/3799038/onsharedpreferencechanged-not-fired-if-change-occurs-in-separate-activity
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this);

        YambaService.startPolling(this);
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences p, String s) {
        Log.d(TAG, "prefs changed");
        client = null;
    }

    public synchronized YambaClient getClient() {
        if (null == client) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            String usr = prefs.getString(userKey, null);
            String pwd = prefs.getString(pwdKey, null);
            String uri = prefs.getString(uriKey, null);
            Log.d(TAG, "new client: " + usr + "@" + uri);

            try { client = new YambaClient(usr, pwd, uri); }
            catch (Exception e) {
                Log.e(TAG, "failed creating client: " + e, e);
            }
        }

        return client;
    }
}
