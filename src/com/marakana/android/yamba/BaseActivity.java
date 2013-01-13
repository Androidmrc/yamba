package com.marakana.android.yamba;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


public class BaseActivity extends Activity {
    private static final String TAG = "BASE";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (BuildConfig.DEBUG) { Log.d(TAG, "menu selected"); }
        switch (id) {
            case R.id.itemTimeline:
                startActivity(new Intent(this, TimelineActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;

            case R.id.itemStatus:
                startActivity(new Intent(this, StatusActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                break;

            case R.id.itemPrefs:
                startActivity(new Intent(this, PrefsActivity.class));
                return true;

            default:
                Log.w(TAG, "Unrecognized menu item: " + item);
                return false;
        }

        return true;
    }
}
