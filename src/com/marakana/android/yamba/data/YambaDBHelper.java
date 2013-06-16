package com.marakana.android.yamba.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class YambaDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE = "yamba.db";
    public static final int VERSION = 4;

    public static final String TABLE = "ytimeline";
    public static final String COL_ID = "yid";
    public static final String COL_TIMESTAMP = "ytimestamp";
    public static final String COL_USER = "yuser";
    public static final String COL_STATUS = "ystatus";

    public YambaDBHelper(Context context) {
        super(context, DATABASE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
         db.execSQL(
                "CREATE TABLE " + TABLE + "("
                        + COL_ID + " INTEGER PRIMARY KEY,"
                        + COL_TIMESTAMP + " INTEGER,"
                        + COL_USER + " TEXT,"
                        + COL_STATUS + " TEXT)");
     }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }
}
