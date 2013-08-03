package com.marakana.android.yamba;

import android.os.Bundle;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class TimelineFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private static final String TAG = "TIMELINE";

    private static final int TIMELINE_LOADER = 666;

    private static final String[] PROJ = {
        YambaContract.Timeline.Column.ID,
        YambaContract.Timeline.Column.TIMESTAMP,
        YambaContract.Timeline.Column.USER,
        YambaContract.Timeline.Column.STATUS,
    };

    private static final String[] FROM = new String[PROJ.length - 1];
    static { System.arraycopy(PROJ, 1, FROM, 0, FROM.length); }

    private static final int[] TO = {
        R.id.timeline_time,
        R.id.timeline_user,
        R.id.timeline_status,
    };

    static class TimelineBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor c, int idx) {
            if (R.id.timeline_time != view.getId()) { return false; }

            CharSequence s = "long ago";
            long t = c.getLong(idx);
            if (0 < t) { s = DateUtils.getRelativeTimeSpanString(t); }
            ((TextView) view).setText(s);
            return true;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "create loader");
        return new CursorLoader(
                getActivity(),
                YambaContract.Timeline.URI,
                PROJ,
                null,
                null,
                YambaContract.Timeline.Column.TIMESTAMP + " DESC" );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> l, Cursor c) {
        Log.d(TAG, "load finished: " + c.getCount());
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> c) {
        Log.d(TAG, "load reset");
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle state) {
        View v = super.onCreateView(inflater, root,  state);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.timeline_row,
                null,
                FROM,
                TO,
                0);

        adapter.setViewBinder(new TimelineBinder());
        setListAdapter(adapter);

        getLoaderManager().initLoader(TIMELINE_LOADER, null, this);

        return v;
    }

    @Override
    public void onListItemClick(ListView l, View v, int p, long id) {
        Cursor c = (Cursor) l.getItemAtPosition(p);

        Intent i = new Intent(getActivity(), TimelineDetailActivity.class);
        i.putExtras(TimelineDetailFragment.bundleDetails(
                c.getLong(c.getColumnIndex(YambaContract.Timeline.Column.TIMESTAMP)),
                c.getString(c.getColumnIndex(YambaContract.Timeline.Column.USER)),
                c.getString(c.getColumnIndex(YambaContract.Timeline.Column.STATUS))));

        startActivity(i);
    }
}
