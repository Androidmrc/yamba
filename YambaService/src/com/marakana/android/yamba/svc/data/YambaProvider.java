package com.marakana.android.yamba.svc.data;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.marakana.android.yamba.svc.BuildConfig;
import com.marakana.android.yamba.svc.Poster;
import com.marakana.android.yamba.svc.YambaContract;
import com.marakana.android.yamba.svc.YambaService;


/**
 * YambaProvider
 */
public class YambaProvider extends ContentProvider {
    private static final String TAG = "CP";

    private static final int POST_DIR = 1;
    private static final int POST_ITEM = 2;
    private static final int TIMELINE_DIR = 3;
    private static final int TIMELINE_ITEM = 4;
    private static final int SERVICE_DIR = 5;

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(
                ServiceContract.AUTHORITY,
                ServiceContract.TABLE,
                SERVICE_DIR);
        uriMatcher.addURI(
                YambaContract.AUTHORITY,
                YambaContract.Posts.TABLE,
                POST_DIR);
        uriMatcher.addURI(
                YambaContract.AUTHORITY,
                YambaContract.Posts.TABLE + "/#",
                POST_ITEM);
       uriMatcher.addURI(
                YambaContract.AUTHORITY,
                YambaContract.Timeline.TABLE,
                TIMELINE_DIR);
        uriMatcher.addURI(
                YambaContract.AUTHORITY,
                YambaContract.Timeline.TABLE + "/#",
                TIMELINE_ITEM);
    }

    private static final ProjectionMap PROJ_MAP_SERVICE = new ProjectionMap.Builder()
        .addColumn(ServiceContract.Columns.MAX_TIMESTAMP, "max(" + YambaHelper.COL_TIMESTAMP + ")")
        .build();

    private static final ProjectionMap PROJ_MAP_POSTS = new ProjectionMap.Builder()
        .addColumn(YambaContract.Posts.Columns.ID, YambaHelper.COL_ID)
        .addColumn(YambaContract.Posts.Columns.TIMESTAMP, YambaHelper.COL_TIMESTAMP)
        .addColumn(YambaContract.Timeline.Columns.STATUS, YambaHelper.COL_STATUS)
        .build();

    private static final ProjectionMap PROJ_MAP_TIMELINE = new ProjectionMap.Builder()
        .addColumn(YambaContract.Timeline.Columns.ID, YambaHelper.COL_ID)
        .addColumn(YambaContract.Timeline.Columns.TIMESTAMP, YambaHelper.COL_TIMESTAMP)
        .addColumn(YambaContract.Timeline.Columns.USER, YambaHelper.COL_USER)
        .addColumn(YambaContract.Timeline.Columns.STATUS, YambaHelper.COL_STATUS)
        .build();

    private static final ColumnMap COL_MAP_POSTS = new ColumnMap.Builder()
        .addColumn(
                YambaContract.Posts.Columns.STATUS,
                YambaHelper.COL_STATUS,
                ColumnMap.Type.STRING)
        .addColumn(
                YambaContract.Posts.Columns.TIMESTAMP,
                YambaHelper.COL_TIMESTAMP,
                ColumnMap.Type.STRING)
        .build();

    private static final ColumnMap COL_MAP_SERVICE = new ColumnMap.Builder()
        .addColumn(
                ServiceContract.Columns.ID,
                YambaHelper.COL_ID,
                ColumnMap.Type.LONG)
        .addColumn(
                ServiceContract.Columns.TRANSACTION,
                YambaHelper.COL_XACT,
                ColumnMap.Type.STRING)
        .addColumn(
                ServiceContract.Columns.TIMESTAMP,
                YambaHelper.COL_TIMESTAMP,
                ColumnMap.Type.LONG)
        .addColumn(
                ServiceContract.Columns.USER,
                YambaHelper.COL_USER,
                ColumnMap.Type.STRING)
        .addColumn(
                ServiceContract.Columns.STATUS,
                YambaHelper.COL_STATUS,
                ColumnMap.Type.STRING)
        .build();


    private YambaHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new YambaHelper(getContext());
        return dbHelper != null;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case TIMELINE_ITEM:
                return YambaContract.Timeline.ITEM_TYPE;
            case TIMELINE_DIR:
                return YambaContract.Timeline.DIR_TYPE;
            case POST_ITEM:
                return YambaContract.Posts.ITEM_TYPE;
            case POST_DIR:
                return YambaContract.Posts.DIR_TYPE;
            default:
                return null;
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("fallthrough")
    @Override
    public Cursor query(Uri uri, String[] proj, String sel, String[] selArgs, String sort) {
        ProjectionMap pm;
        String table;
        long pk = -1;

        if (BuildConfig.DEBUG) { Log.d(TAG, "query: " + uri); }
    	switch (uriMatcher.match(uri)) {
	    	case TIMELINE_ITEM:
	    		pk = ContentUris.parseId(uri);

	    	case TIMELINE_DIR:
                table = YambaHelper.TABLE_TIMELINE;
                pm = PROJ_MAP_TIMELINE;
	    		break;

	    	case POST_ITEM:
                pk = ContentUris.parseId(uri);

            case POST_DIR:
                table = YambaHelper.TABLE_POSTS;
                pm = PROJ_MAP_POSTS;
                break;

            case SERVICE_DIR:
                table = YambaHelper.TABLE_TIMELINE;
                pm = PROJ_MAP_SERVICE;
                break;

	    	default:
	    		throw new IllegalArgumentException("URI unsupported in query: " + uri);
    	}

    	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            qb.setStrict(true);
        }

        qb.setProjectionMap(pm.getProjectionMap());

        qb.setTables(table);

        if (0 <= pk) { qb.appendWhere(YambaHelper.COL_ID + "=" + pk); }

        Cursor c = qb.query(getDb(), proj, sel, selArgs, null, null, sort);

        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues vals) {
        String table;

        if (BuildConfig.DEBUG) { Log.d(TAG, "insert: " + uri); }
        switch (uriMatcher.match(uri)) {
            case POST_DIR:
                table = YambaHelper.TABLE_POSTS;
                vals = COL_MAP_POSTS.translateCols(vals);
                postRemote(vals);
                break;

            default:
                throw new IllegalArgumentException("URI unsupported in insert: " + uri);
        }

        long pk = getDb().insertOrThrow(table, null, vals);
        if (0 >= pk) { return null; }

        uri = uri.buildUpon().appendPath(String.valueOf(pk)).build();

        getContext().getContentResolver().notifyChange(uri, null);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] vals) {
        String table;
        ColumnMap cm;

        if (BuildConfig.DEBUG) { Log.d(TAG, "bulk insert: " + uri); }

        switch (uriMatcher.match(uri)) {
            case SERVICE_DIR:
                table = YambaHelper.TABLE_TIMELINE;
                cm = COL_MAP_SERVICE;
                break;

            default:
                throw new UnsupportedOperationException("URI unsupported in bulk insert: " + uri);
        }

        SQLiteDatabase db = getDb();
        int count = 0;
        try {
            db.beginTransaction();
            for (ContentValues row: vals) {
                if (0 < db.insert(table, null, cm.translateCols(row))) { count++; }
            }
            db.setTransactionSuccessful();
        }
        finally { db.endTransaction(); }

        if (0 < count) {
            getContext().getContentResolver().notifyChange(YambaContract.Timeline.URI, null);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues vals, String where, String[] whereArgs) {
        String table;
        int n;

        if (BuildConfig.DEBUG) { Log.d(TAG, "build insert: " + uri); }
        switch (uriMatcher.match(uri)) {
            case SERVICE_DIR:
                vals = COL_MAP_SERVICE.translateCols(vals);
                table = YambaHelper.TABLE_POSTS;
                break;

            default:
                throw new IllegalArgumentException("URI unsupported in update: " + uri);
        }

        n = getDb().update(table, vals, where, whereArgs);

        if (0 < n) {
            getContext().getContentResolver().notifyChange(YambaContract.Posts.URI, null);
        }

        return n;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        throw new IllegalArgumentException("Unsupported operation: delete");
    }

    private void postRemote(ContentValues vals) {
        String xact = YambaService.getTransactionId();
        vals.put(YambaHelper.COL_XACT, xact);
        Poster.postStatus(
                getContext(),
                xact,
                vals.getAsString(YambaHelper.COL_STATUS));
    }

    private SQLiteDatabase getDb() { return dbHelper.getWritableDatabase(); }
}