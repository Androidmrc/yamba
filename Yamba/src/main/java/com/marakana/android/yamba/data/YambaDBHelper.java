package com.marakana.android.yamba.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class YambaDBHelper extends SQLiteOpenHelper {
    private static final String DB_FILE = "yamba.db";
    private static final int VERSION = 1;

    public static final String TABLE = "timeline";
    public static final class Column {
        private Column() { }

        public static final String ID = "id";
        public static final String TIMESTAMP = "createdAt";
        public static final String USER = "user";
        public static final String STATUS = "status";
    }

    public YambaDBHelper(Context ctxt) {
        super(ctxt, DB_FILE, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ("
                + Column.ID + " INTEGER PRIMARY KEY,"
                + Column.TIMESTAMP + " INTEGER NOT NULL,"
                + Column.USER + " TEXT NOT NULL,"
                + Column.STATUS + " TEXT NOT NULL)"
            );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE " + TABLE);
        onCreate(db);
    }
}
