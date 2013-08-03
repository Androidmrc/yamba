package com.marakana.android.yamba;

import android.app.Fragment;
import android.content.res.Resources;
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

import com.marakana.android.yamba.svc.YambaService;


public class StatusFragment extends Fragment {
    private static final String TAG = "STATUS";

    private TextView count;
    private EditText status;

    private int okColor;
    private int warnColor;
    private int errColor;

    private int maxStatusLen;
    private int warnMax;
    private int errMax;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle state) {
        Resources rez = getResources();

        okColor = rez.getColor(R.color.status_ok);
        warnColor = rez.getColor(R.color.status_warn);
        errColor = rez.getColor(R.color.status_err);

        maxStatusLen = rez.getInteger(R.integer.status_max_len);
        warnMax = rez.getInteger(R.integer.warn_max);
        errMax = rez.getInteger(R.integer.err_max);

       View v = inflater.inflate(R.layout.status_fragment, root, false);

        v.findViewById(R.id.status_submit).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) { post(); }
            } );

        count = (TextView) v.findViewById(R.id.status_count);
        status = (EditText) v.findViewById(R.id.status_status);
        status.addTextChangedListener(
            new TextWatcher() {

                @Override
                public void afterTextChanged(Editable str) { updateCount(); }

                @Override
                public void beforeTextChanged(CharSequence str, int s, int c, int a) { }

                @Override
                public void onTextChanged(CharSequence str, int s, int c, int a) { }
            } );

        return v;
    }

    void updateCount() {
        int n = maxStatusLen - status.getText().length();

        int c;
        if (n > warnMax) { c = okColor; }
        else if (n > errMax) { c = warnColor; }
        else { c = errColor; }

        count.setText(String.valueOf(n));
        count.setTextColor(c);
    }

    void post() {
        String msg = status.getText().toString();
        Log.d(TAG, "posting: " + msg);

        if (TextUtils.isEmpty(msg)) { return; }

        YambaService.post(getActivity(), msg);

        status.setText("");
    }
}
