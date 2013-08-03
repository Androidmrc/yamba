package com.marakana.android.yamba;

import android.app.Fragment;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TimelineDetailFragment extends Fragment {

    public static Fragment newInstance(Bundle args) {
        TimelineDetailFragment frag = new TimelineDetailFragment();
        frag.setArguments(args);
        return frag;
    }

    public static Bundle bundleDetails(long ts, String user, String status) {
        Bundle args = new Bundle();
        args.putLong(YambaContract.Timeline.Column.TIMESTAMP, ts);
        args.putString(YambaContract.Timeline.Column.USER, user);
        args.putString(YambaContract.Timeline.Column.STATUS, status);
        return args;
    }


    private View details;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle state) {
        details = inflater.inflate(R.layout.timeline_detail_fragment, root, false);

        // I'm not sure that this is necessary.
        // Android should restore the the state, automatically, under some circs.
        setDetails(getArguments());

        return details;
    }

    public void setDetails(Bundle args) {
        if ((null == args) || (null == details)) { return; }

        ((TextView) details.findViewById(R.id.timeline_detail_timestamp))
            .setText(DateUtils.getRelativeTimeSpanString(
                    args.getLong(YambaContract.Timeline.Column.TIMESTAMP, 0L)));
        ((TextView) details.findViewById(R.id.timeline_detail_user)).setText(
                args.getString(YambaContract.Timeline.Column.USER));
        ((TextView) details.findViewById(R.id.timeline_detail_status)).setText(
                args.getString(YambaContract.Timeline.Column.STATUS));
    }
}
