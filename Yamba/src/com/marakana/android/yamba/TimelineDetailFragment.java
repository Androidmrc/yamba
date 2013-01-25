package com.marakana.android.yamba;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TimelineDetailFragment extends Fragment {
    public static final String TAG = "DETAIL";

    public static final TimelineDetailFragment newInstance() {
        return newInstance(new Bundle());
    }

    public static final TimelineDetailFragment newInstance(Bundle init) {
        TimelineDetailFragment frag = new TimelineDetailFragment();
        frag.setArguments(init);
        return frag;
    }


    private String text;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        if (null == state) { state = getArguments(); }

        text = (null == state)
                ? null
                : state.getString(TimelineActivity.TAG_TEXT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b) {
        View view = inflater.inflate(
            R.layout.timeline_detail,
            container,
            false);  //!!! this is important

        if (BuildConfig.DEBUG) { Log.d(TAG, "details: " + text); }
        if (null != text) { ((TextView) view).setText(text); }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (null != text) { state.putString(TimelineActivity.TAG_TEXT, text); }
    }
}
