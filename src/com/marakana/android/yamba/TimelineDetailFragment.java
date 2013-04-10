package com.marakana.android.yamba;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TimelineDetailFragment extends Fragment {
    public static final String TAG = "DETAIL";

    /**
     * Static constructor
     */
    public static final TimelineDetailFragment newInstance(Bundle init) {
        if (null == init) { init = new Bundle(); }
        TimelineDetailFragment frag = new TimelineDetailFragment();
        frag.setArguments(init);
        return frag;
    }


    private TextView detailsView;
    private String details;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if (null == state) { state = getArguments(); }
        setContent(state);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        // the "false" down there is important!
        detailsView = (TextView) inflater.inflate(R.layout.timeline_detail, container, false);
        setContent(state);
        return detailsView;
     }

    public void setContent(Bundle state) {
        if (null != state) { details = state.getString(TimelineActivity.TAG_TEXT); }
        if (null != detailsView) { detailsView.setText(details); }
    }
}
