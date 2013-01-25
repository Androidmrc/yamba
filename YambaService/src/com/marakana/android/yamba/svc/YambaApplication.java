package com.marakana.android.yamba.svc;


import java.util.List;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.marakana.android.yamba.clientlib.YambaClient;
import com.marakana.android.yamba.clientlib.YambaClientException;


/**
 * YambaApplication
 */
public class YambaApplication
    extends Application
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "APP";

    public static final String DEFAULT_USER = "student";
    public static final String DEFAULT_PASSWORD = "password";
    public static final String DEFAULT_API_ROOT = "http://yamba.marakana.com/api";

    public static class SafeYambaClient {
        public static final int MAX_POSTS = 40;

        private final YambaClient rawClient;

        public SafeYambaClient(String usr, String pwd, String endpoint) {
            rawClient = new YambaClient(usr, pwd, endpoint);
        }

        public synchronized List<YambaClient.Status> getTimeline() throws YambaClientException {
            return rawClient.getTimeline(MAX_POSTS);
       }

        public synchronized void postStatus(String statusText) throws YambaClientException {
            rawClient.postStatus(statusText);
       }
    }


    private String keyUser;
    private String keyPasswd;
    private String keyEndpoint;
    private SafeYambaClient client;

    /**
     * @see android.app.Application#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) { Log.d(TAG, "Application up!"); }

        Resources rez = getResources();
        keyUser = rez.getString(R.string.prefs_key_user);
        keyPasswd = rez.getString(R.string.prefs_key_pwd);
        keyEndpoint = rez.getString(R.string.prefs_key_endpoint);

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Don't use an anonymous class to handle this event!
     * http://stackoverflow.com/questions/3799038/onsharedpreferencechanged-not-fired-if-change-occurs-in-separate-activity
     */
    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences key, String val) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "prefs changed"); }
        client = null;
    }

    public synchronized SafeYambaClient getClient() {
        if (null == client) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String user = prefs.getString(keyUser, DEFAULT_USER);
            String passwd = prefs.getString(keyPasswd, DEFAULT_PASSWORD);
            String endpoint = prefs.getString(keyEndpoint, DEFAULT_API_ROOT);

            client = new SafeYambaClient(user, passwd, endpoint);
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "new client: " + user + ", " + passwd  + ", " + endpoint);
            }
        }
        return client;
    }
}
