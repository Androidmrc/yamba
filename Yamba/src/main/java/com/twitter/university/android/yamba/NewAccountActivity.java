package com.twitter.university.android.yamba;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class NewAccountActivity extends AccountAuthenticatorActivity {
    private static final String TAG = "ACCOUNT";

    private class EmptyWatcher implements TextWatcher {
        private final int id;

        EmptyWatcher(int id) {
            this.id = id;
            setEmpty(id, true);
        }

        @Override
        public void afterTextChanged(Editable s) { setEmpty(id, 0 >= s.length()); }

        @Override
        public void beforeTextChanged(CharSequence s, int b, int n, int a) { }

        @Override
        public void onTextChanged(CharSequence s, int b, int p, int n) { }
    }


    private final SparseArray<Boolean> empty = new SparseArray<Boolean>();
    private EditText handle;
    private EditText password;
    private EditText endpoint;
    private Button submit;
    private int pollInterval;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        setContentView(R.layout.activity_new_account);

        pollInterval = getResources().getInteger(R.integer.poll_interval) * 60 * 1000;

        submit = (Button) findViewById(R.id.account_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { newAccount(); }
        });

        handle = (EditText) findViewById(R.id.account_handle);
        handle.addTextChangedListener(new EmptyWatcher(R.id.account_handle));

        password = (EditText) findViewById(R.id.account_password);
        password.addTextChangedListener(new EmptyWatcher(R.id.account_password));

        endpoint = (EditText) findViewById(R.id.account_endpoint);
        endpoint.addTextChangedListener(new EmptyWatcher(R.id.account_endpoint));
    }

    void setEmpty(int id, boolean isEmpty) {
        empty.put(id, isEmpty);
        submit.setEnabled(valid());
    }

    private void newAccount() {
        Bundle bundle = getIntent().getExtras();
        if (BuildConfig.DEBUG) { Log.d(TAG, "create account: " + bundle); }

        if (!valid()) { return; }

        createAccount(
            bundle.getString(AccountManager.KEY_ACCOUNT_TYPE),
            handle.getText().toString(),
            password.getText().toString(),
            endpoint.getText().toString(),
            bundle
        );

        ((AccountAuthenticatorResponse) bundle
            .getParcelable(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE))
                .onResult(bundle);

        finish();
    }

    private void createAccount(
        String accountType,
        String handle,
        String password,
        String endpoint,
        Bundle bundle)
    {
        String acctName = new StringBuilder().append(handle).append("@").append(endpoint).toString();
        if (BuildConfig.DEBUG) {
            Log.d( TAG, "create account: " + accountType + ": " + acctName);
        }

        Account account = new Account(acctName, accountType);
        if (!AccountManager.get(this).addAccountExplicitly(account, password, null)) {
            bundle.putInt(AccountManager.KEY_ERROR_CODE, -1);
            bundle.putString(
                AccountManager.KEY_ERROR_MESSAGE,
                "Unable to create account");
            return;
        }

        String authority = getString(R.string.yamba_authority);
        ContentResolver.setIsSyncable(account, authority, 1);
        ContentResolver.setSyncAutomatically(account, authority, true);
        ContentResolver.addPeriodicSync(
            account,
            authority,
            null,
            pollInterval);
    }

    private boolean valid() {
        for (int i = 0; i < empty.size(); i++) {
            if (empty.valueAt(i).booleanValue()) { return false; }
        }
        return true;
    }
}


