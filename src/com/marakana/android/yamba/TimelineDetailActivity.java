package com.marakana.android.yamba;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class TimelineDetailActivity extends BaseActivity {
    public static final String TAG = "DETAILFRAG";


    private TextView detail;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.timeline_detail);
        detail = (TextView) findViewById(R.id.timeline_detail);

        if (null == state) { state = getIntent().getExtras(); }

        if (null != state) {
            String text = state.getString(TimelineActivity.TAG_TEXT);
            if (BuildConfig.DEBUG) { Log.d(TAG, "details: " + text); }
            if (null != text) { detail.setText(text); }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (null != detail) {
            CharSequence text = detail.getText();
            if (null != text) {
                state.putString(TimelineActivity.TAG_TEXT, text.toString());
            }
        }
    }
}
