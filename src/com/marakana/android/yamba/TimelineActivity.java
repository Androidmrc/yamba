package com.marakana.android.yamba;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.marakana.android.yamba.svc.Poller;


public class TimelineActivity extends BaseActivity {
    private static final String TAG = "TIME";

    public static final String TAG_TEXT = "TimelineActivity.TEXT";
    private static final String FRAG_TAG = "TimelineActivity.DETAILS";


    private boolean useFrag;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_timeline:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "start activity: " + useFrag); }
        if (!useFrag) { startActivity(intent); }
        else if (fragment instanceof TimelineFragment) {
            launchDetailFragment(intent.getExtras());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        useFrag = null != findViewById(R.id.timeline_detail);

        if (useFrag) { installDetailsFragment(); }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Poller.stopPolling(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Poller.startPolling(getApplicationContext());
    }

    private void installDetailsFragment() {
        FragmentManager fragMgr = getFragmentManager();

        if (null != fragMgr.findFragmentByTag(FRAG_TAG)) { return; }

        FragmentTransaction xact = fragMgr.beginTransaction();
        xact.add(
            R.id.timeline_detail,
            TimelineDetailFragment.newInstance(),
            FRAG_TAG);
        xact.commit();
    }

    private void launchDetailFragment(Bundle xtra) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "launching fragment"); }

        FragmentTransaction xact = getFragmentManager().beginTransaction();

        xact.replace(
            R.id.timeline_detail,
            TimelineDetailFragment.newInstance(xtra),
            FRAG_TAG);

        xact.addToBackStack(null);
        xact.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        xact.commit();
    }
}
