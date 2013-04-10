package com.marakana.android.yamba;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class StatusFragment extends Fragment {
    public static final String TAG = "STATUS";

    private static final int MIN_CHARS = 0;
    private static final int WARN_CHARS = 10;
    private static final int MAX_CHARS = 140;

    static Poster poster;

    // if two different threads access mutable state
    // all access must be performed holding a single lock.
    static class Poster extends AsyncTask<String, Void, Void> {
        private final ContentResolver resolver;

        public Poster(ContentResolver resolver) { this.resolver = resolver; }

        @Override
        protected Void doInBackground(String... args) {
            String status = args[0];
            if (BuildConfig.DEBUG) { Log.d(TAG, "posting status: " + status); }

            ContentValues vals = new ContentValues();
            vals.put(YambaContract.Posts.Columns.STATUS, status);

            resolver.insert(YambaContract.Posts.URI, vals);

            return null;
        }

        @Override
        protected void onCancelled() { cleanup(); }

        @Override
        protected void onPostExecute(Void result) { cleanup(); }

        private void cleanup() { poster = null; }
    }


    private TextView textCount;
    private EditText editText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle state) {
        super.onCreateView(inflater, parent, state);

        View root = inflater.inflate(R.layout.status, parent, false);

        textCount = (TextView) root.findViewById(R.id.count_text);

        editText = (EditText) root.findViewById(R.id.status_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) { updateStatusLen(); }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        root.findViewById(R.id.status_button).setOnClickListener(
                new Button.OnClickListener() {
                    @Override public void onClick(View v) { update(); }
                } );

        return root;
    }

    void update() {
        // only allow one post in flight
        if (poster != null) { return; }

        String msg = editText.getText().toString();
        if (BuildConfig.DEBUG) { Log.d(TAG, "update: " + msg); }

        if (TextUtils.isEmpty(msg)) { return; }

        editText.setText("");
        //YambaService.post(getActivity(), status);

        poster = new Poster(getActivity().getContentResolver());
        poster.execute(msg);
    }

    void updateStatusLen() {
        int remaining = MAX_CHARS - editText.getText().length();

        int color;
        if (remaining <= MIN_CHARS) { color = Color.RED; }
        else if (remaining <= WARN_CHARS) { color = Color.YELLOW; }
        else { color = Color.GREEN; }

        textCount.setText(String.valueOf(remaining));
        textCount.setTextColor(color);
    }
}
