package com.twitter.university.android.yamba.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


class YambaDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DB";

    public static final String DATABASE = "yamba.db";
    public static final int VERSION = 1;

    public YambaDbHelper(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "create db");
        db.execSQL(
            "CREATE TABLE " + YambaContract.Timeline.TABLE + "("
                + YambaContract.Timeline.Columns.ID + " INTEGER PRIMARY KEY,"
                + YambaContract.Timeline.Columns.TIMESTAMP + " INTEGER NOT NULL,"
                + YambaContract.Timeline.Columns.HANDLE + " STRING NOT NULL,"
                + YambaContract.Timeline.Columns.TWEET + " STRING NOT NULL" + ")");
        db.execSQL(
            "CREATE TABLE " + YambaContract.Posts.TABLE + "("
                + YambaContract.Posts.Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + YambaContract.Posts.Columns.TIMESTAMP + " INTEGER NOT NULL,"
                + YambaContract.Posts.Columns.TRANSACTION + " STRING DEFAULT(NULL),"
                + YambaContract.Posts.Columns.SENT + " STRING DEFAULT(NULL),"
                + YambaContract.Posts.Columns.TWEET + " STRING NOT NULL" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "update db");
        db.execSQL("DROP TABLE IF EXISTS " + YambaContract.Timeline.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + YambaContract.Posts.TABLE);
        onCreate(db);
    }
}
