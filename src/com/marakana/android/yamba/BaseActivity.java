package com.marakana.android.yamba;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


@SuppressLint("Registered")
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

        if (id == android.R.id.home || id == R.id.item_timeline) {
            newPage(TimelineActivity.class, true);
            return true;
        }

        if (id == R.id.item_status) {
            newPage(StatusActivity.class, true);
            return true;
        }

        if (id == R.id.item_prefs) {
            newPage(PrefsActivity.class, false);
            return true;
        }

        if (id == R.id.item_about) {
            Toast.makeText(this, R.string.about, Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setHomeButtonEnabled(true);
        }
    }

    private void newPage(Class<?> page, boolean reorder) {
        Intent i = new Intent(this, page);
        if (reorder) { i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); }
        startActivity(i);
    }
}
