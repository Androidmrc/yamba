package com.marakana.android.yamba;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.marakana.android.yamba.svc.YambaSvc;


public class StatusFragment extends Fragment  {
    private static final String TAG = "STATUS";

    private static final int MAX_CHARS = 140;
    private static final int WARN_CHARS = 10;
    private static final int ERROR_CHARS = 0;

//    static Poster poster;
//
//    private static class Poster extends AsyncTask<String, Void, Integer> {
//        private final YambaApp ctxt;
//
//        public Poster(YambaApp ctxt) { this.ctxt = ctxt; }
//
//        @Override
//        protected Integer doInBackground(String... status) {
//            int succ = R.string.fail;
//            try {
//                ctxt.getClient().postStatus(status[0]);
//                succ = R.string.succeed;
//            }
//            catch (YambaClientException e) {
//                Log.w(TAG, "post failed", e);
//            }
//            return Integer.valueOf(succ);
//        }
//
//        @Override
//        protected void onCancelled() { done(R.string.fail); }
//
//        @Override
//        protected void onPostExecute(Integer result) { done(result.intValue()); }
//
//        private void done(int succ) {
//            poster = null;
//            Toast.makeText(ctxt, succ, Toast.LENGTH_LONG).show();
//        }
//    }


    private int colorOk;
    private int colorWarn;
    private int colorError;

    private TextView countView;
    private EditText statusView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "create activity: " + this); }

        colorOk = getResources().getColor(R.color.ok);
        colorWarn = getResources().getColor(R.color.warn);
        colorError = getResources().getColor(R.color.error);

        View v = inflater.inflate(R.layout.fragment_status, container, false);

        v.findViewById(R.id.status_submit).setOnClickListener(
                new View.OnClickListener() {
                    @Override public void onClick(View view) { post(); }
                });
        countView = (TextView) v.findViewById(R.id.status_count);
        statusView = (EditText) v.findViewById(R.id.status_status);
        statusView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) { updateCount(); }

            @Override
            public void beforeTextChanged(CharSequence s, int a1, int a2, int a3) { }

            @Override
            public void onTextChanged(CharSequence s, int a1, int a2, int a3) { }
        });

        return v;
    }

    void updateCount() {
        int n = MAX_CHARS - statusView.getText().length();
        int color = colorOk;
        if (ERROR_CHARS > n) { color = colorError; }
        else if (WARN_CHARS > n) { color = colorWarn; }
        countView.setText(String.valueOf(n));
        countView.setTextColor(color);
    }

    void post() {
//        if (null != poster) { return; }

        String status = statusView.getText().toString();
        if (TextUtils.isEmpty(status)) { return; }

        statusView.setText("");

        YambaSvc.post(getActivity(), status);
//        poster = new Poster((YambaApp) getActivity().getApplicationContext());
//        poster.execute(status);
    }
}
