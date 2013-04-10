package com.marakana.android.yamba;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;



public class TimelineFragment extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final String TAG = "TIMEFRAG";
    private static final int LOADER_ID = 37;

    private static final String[] PROJ = new String[] {
        YambaContract.Timeline.Columns.ID,
        YambaContract.Timeline.Columns.USER,
        YambaContract.Timeline.Columns.TIMESTAMP,
        YambaContract.Timeline.Columns.STATUS
    };

    private static final String[] FROM = new String[PROJ.length - 1];
    static { System.arraycopy(PROJ, 1, FROM, 0, FROM.length); };

    private static final int[] TO = new int[] {
        R.id.textUser,
        R.id.textTime,
        R.id.textStatus
    };

    static class TimelineBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int colIndex) {
            if (view.getId() != R.id.textTime) { return false; }

            String tStr = "ages ago";
            long t = cursor.getLong(colIndex);
            if (0 < t) {
                tStr = DateUtils.getRelativeTimeSpanString(t, System.currentTimeMillis(), 0)
                        .toString();
            }
            ((TextView) view).setText(tStr);
            return true;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle b) {
        View view = super.onCreateView(inflater, container, b);

        getLoaderManager().initLoader(LOADER_ID, null, this);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.timeline_row,
                null,
                FROM,
                TO,
                0);
        adapter.setViewBinder(new TimelineBinder());
        setListAdapter(adapter);

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "create loader"); }
        return new CursorLoader(
                getActivity().getApplicationContext(),
                YambaContract.Timeline.URI,
                PROJ,
                null,
                null,
                YambaContract.Timeline.Columns.TIMESTAMP + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "loader finished"); }
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "loader reset"); }
        ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int pos, long id) {
        Cursor cur = (Cursor) getListAdapter().getItem(pos);
        String text = cur.getString(cur.getColumnIndex(YambaContract.Timeline.Columns.STATUS));
        if (BuildConfig.DEBUG) { Log.d(TAG, "launch details: " + text); }

        Intent intent = new Intent();
        intent.setClass(getActivity(), TimelineDetailActivity.class);
        intent.putExtra(TimelineActivity.TAG_TEXT, text);

        startActivity(intent);
    }
}
