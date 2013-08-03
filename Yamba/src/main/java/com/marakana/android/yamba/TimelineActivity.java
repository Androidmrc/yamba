package com.marakana.android.yamba;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import com.marakana.android.yamba.svc.YambaService;


public class TimelineActivity extends YambaActivity {
    private static final String DETAIL_FRAGMENT = "Timeline.DETAILS";


    private boolean usingFragments;

    @Override
    public void startActivityFromFragment(Fragment frag, Intent i, int code) {
        if (usingFragments) { launchDetailFragment(i.getExtras()); }
        else { super.startActivityFromFragment(frag, i, code); }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeline_activity);

        usingFragments = (null != findViewById(R.id.timeline_details));

        if (usingFragments) { addDetailFragment(); }
    }

    @Override
    protected void onPause() {
        super.onPause();
        YambaService.stopPolling(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        YambaService.startPolling(this);
    }

    private void addDetailFragment() {
        FragmentManager mgr = getFragmentManager();

        if (null != mgr.findFragmentByTag(DETAIL_FRAGMENT)) { return; }

        FragmentTransaction xact = mgr.beginTransaction();
        xact.add(
                R.id.timeline_details,
                new TimelineDetailFragment(),
                DETAIL_FRAGMENT);
        xact.commit();
    }

    private void launchDetailFragment(Bundle args) {
        FragmentTransaction xact = getFragmentManager().beginTransaction();
        xact.replace(
                R.id.timeline_details,
                TimelineDetailFragment.newInstance(args),
                DETAIL_FRAGMENT);
        xact.addToBackStack(null);
        xact.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        xact.commit();
    }
}
