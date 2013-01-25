package com.marakana.android.yamba.svc.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.marakana.android.yamba.svc.BuildConfig;


/**
 * YambaHelper
 */
class YambaHelper extends SQLiteOpenHelper {
    public static final String TAG = "YambaHelper";

    public static final int VERSION = 1;
    public static final String DATABASE = "yamba.db";

    public static final String TABLE_POSTS = "posts";
    public static final String TABLE_TIMELINE = "timeline";

    public static final String COL_ID = "id";
    public static final String COL_XACT = "xact";
    public static final String COL_TIMESTAMP = "created_at";
    public static final String COL_USER = "user";
    public static final String COL_STATUS = "status";

    public YambaHelper(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqlDb) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "creating database: " + DATABASE); }
        sqlDb.execSQL(
                "CREATE TABLE " + TABLE_POSTS + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_XACT + " TEXT, "
                        + COL_TIMESTAMP + " INTEGER, "
                        + COL_STATUS + " TEXT)");

        sqlDb.execSQL(
                "CREATE TABLE " + TABLE_TIMELINE + " ("
                        + COL_ID + " INTEGER PRIMARY KEY, "
                        + COL_TIMESTAMP + " INTEGER, "
                        + COL_USER + " TEXT, "
                        + COL_STATUS + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqlDb, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) { Log.d(TAG, "upgrading database: " + DATABASE); }
        sqlDb.execSQL("DROP TABLE " + TABLE_POSTS);
        sqlDb.execSQL("DROP TABLE " + TABLE_TIMELINE);
        onCreate(sqlDb);
    }
}