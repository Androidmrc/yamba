package com.marakana.android.yamba;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;


public class TimelineDetailActivity extends BaseActivity {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_timeline_detail);

        if (null == state) { state = getIntent().getExtras(); }
        if (null != state) {
            TimelineDetailFragment detail
                = (TimelineDetailFragment) getFragmentManager()
                    .findFragmentById(R.id.fragment_timeline_detail);
            detail.setContent(state);
        }
    }
}
