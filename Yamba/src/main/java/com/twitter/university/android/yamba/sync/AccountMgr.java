package com.twitter.university.android.yamba.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.twitter.university.android.yamba.BuildConfig;
import com.twitter.university.android.yamba.NewAccountActivity;
import com.twitter.university.android.yamba.R;


public class AccountMgr extends AbstractAccountAuthenticator {
    private static final String TAG = "AUTH";

    public static final String KEY_TOKEN_TYPE = "AccountAuth.TOKEN_TYPE";

    public static final String acctStr(Account account) {
        return "(" + account.name + "," + account.type + ")";
    }


    private final Context ctxt;

    public AccountMgr(Context context) {
        super(context);
        this.ctxt = context;
    }

    @Override
    public Bundle addAccount(
            AccountAuthenticatorResponse resp,
            String accountType,
            String authTokenType,
            String[] requiredFeatures,
            Bundle options)
    {
        if (BuildConfig.DEBUG) {
            Log.d( TAG, "add account: " + accountType + "#" + authTokenType + " @" + resp);
        }

        Bundle reply = new Bundle();

        String at = ctxt.getString(R.string.account_type);
        reply.putString(AccountManager.KEY_ACCOUNT_TYPE, at);

        if (!at.equals(accountType)) {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(
                AccountManager.KEY_ERROR_MESSAGE,
                "Unrecognized account type");
            return reply;
        }

        Intent intent = new Intent(ctxt, NewAccountActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, resp);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType);

        reply.putParcelable(AccountManager.KEY_INTENT, intent);

        return reply;
    }

    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse response,
            Account account,
            String authTokenType,
            Bundle options)
    {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "token request @" + acctStr(account));
        }
//    String token = obtainToken(authTokenType);
//    if (null == token) {
//        bundle.putInt(AccountManager.KEY_ERROR_CODE, -1);
//        bundle.putString(
//            AccountManager.KEY_ERROR_MESSAGE,
//            "Unrecognized token type");
//        return;
//    }
//    bundle.putString(AccountManager.KEY_AUTHTOKEN, token);

        Bundle reply = new Bundle();
        reply.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
        reply.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
        reply.putString(KEY_TOKEN_TYPE, authTokenType);

        if (!ctxt.getString(R.string.account_type).equals(account.type)
                || !ctxt.getString(R.string.app_name).equals(account.name))
        {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(AccountManager.KEY_ERROR_MESSAGE, "Unrecognized account");
            return reply;
        }

        String token = obtainToken(authTokenType);
        if (null == token) {
            reply.putInt(AccountManager.KEY_ERROR_CODE, -1);
            reply.putString(AccountManager.KEY_ERROR_MESSAGE, "Unrecognized token type");
            return reply;
        }

        reply.putString(AccountManager.KEY_AUTHTOKEN, token);
        return reply;
    }

    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse response,
            Account account,
            String authTokenType,
            Bundle options)
    {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse response,
            Account account,
            String[] features)
    {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse response,
            Account account,
            Bundle options)
    {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    @Override
    public Bundle editProperties(
            AccountAuthenticatorResponse response,
            String accountType)
    {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        throw new UnsupportedOperationException("Update credentials not supported.");
    }

    // incidentally, we use the installation id as the authentication token
    private String obtainToken(String tt) {
        String token = (!ctxt.getString(R.string.token_type).equals(tt))
                ? null
                : null; //id.getInstallationId();
        if (BuildConfig.DEBUG) { Log.d(TAG, "token @" + tt + ": " + token); }
        return token;
   }
}
