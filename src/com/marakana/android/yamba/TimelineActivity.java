package com.marakana.android.yamba;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.marakana.android.yamba.svc.YambaService;


public class TimelineActivity extends BaseActivity {
    private static final String TAG = "TIME";

    public static final String TAG_TEXT = "TimelineActivity.TEXT";

    private static final String FRAG_TAG = "TimelineActivity.DETAILS";


    private boolean useFrag;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (item.getItemId() == R.id.item_timeline)
            ? true
            : super.onOptionsItemSelected(item);
    }

    @Override
    public void startActivityFromFragment(Fragment fragment, Intent intent, int req) {
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
        YambaService.stopPolling(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        YambaService.startPolling(this);
    }

    private void installDetailsFragment() {
        FragmentManager fragMgr = getFragmentManager();

        if (null != fragMgr.findFragmentByTag(FRAG_TAG)) { return; }

        FragmentTransaction xact = fragMgr.beginTransaction();
        xact.add(
            R.id.timeline_detail,
            TimelineDetailFragment.newInstance(null),
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
