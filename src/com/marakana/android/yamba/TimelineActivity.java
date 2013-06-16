/* $Id: $
   Copyright 2013, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.marakana.android.yamba;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import com.marakana.android.yamba.svc.YambaSvc;


/**
 *
 * @version $Revision: $
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 */
public class TimelineActivity extends BaseActivity {
    public static final String TAG_FRAG = "TimelineActivity.DETAILS";

    private boolean usingFragments;

    @Override
    public void startActivityFromFragment(Fragment frag, Intent intent, int code, Bundle opts) {
        if (!usingFragments) { startActivity(intent); }
        else { launchDetailFragment(intent.getStringExtra(TimelineDetailFragment.PARAM_TEXT)); }
    }

    @Override
    protected void onPause() {
        super.onPause();
        YambaSvc.stopPolling(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        YambaSvc.startPolling(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        usingFragments = null != findViewById(R.id.fragment_timeline_details);

        if (usingFragments) { addDetailsFragment(); }
    }

    private void addDetailsFragment() {
        FragmentManager mgr = getFragmentManager();

        if (null != mgr.findFragmentByTag(TAG_FRAG)) { return; }

        FragmentTransaction xact = mgr.beginTransaction();
        xact.add(
                R.id.fragment_timeline_details,
                TimelineDetailFragment.newInstance(null),
                TAG_FRAG);
        xact.commit();
    }

    private void launchDetailFragment(String status) {
        FragmentTransaction xact = getFragmentManager().beginTransaction();
        xact.replace(
                R.id.fragment_timeline_details,
                TimelineDetailFragment.newInstance(status),
                TAG_FRAG);
        xact.addToBackStack(null);
        xact.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        xact.commit();
    }
}
