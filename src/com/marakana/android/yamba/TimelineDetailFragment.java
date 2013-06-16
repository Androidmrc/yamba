package com.marakana.android.yamba;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TimelineDetailFragment extends Fragment {
    public static final String TAG = "DETAIL";
    public static final String PARAM_TEXT = "TimelineActivity.TEXT";

    // Static constructor
    public static TimelineDetailFragment newInstance(String status) {
        TimelineDetailFragment frag = new TimelineDetailFragment();
        if (null != status) {
            Bundle b = new Bundle();
            b.putString(PARAM_TEXT, status);
            frag.setArguments(b);
        }
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
        detailsView = (TextView) inflater.inflate(R.layout.fragment_timeline_detail, container, false);
        setContent(state);
        return detailsView;
     }

    public void setContent(Bundle state) {
        if (null != state) { details = state.getString(PARAM_TEXT); }
        if (null != detailsView) { detailsView.setText(details); }
    }
}
