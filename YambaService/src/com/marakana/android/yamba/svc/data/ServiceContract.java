package com.marakana.android.yamba.svc.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.marakana.android.yamba.svc.YambaContract;


/**
 * Internal Contract for the Yamba Service
 */
public final class ServiceContract {
    private ServiceContract() {}

    public static final String AUTHORITY = YambaContract.AUTHORITY;
    public static final String TABLE = "service";

    public static final Uri URI = YambaContract.BASE_URI.buildUpon().appendPath(TABLE).build();

    public static final String XACT_CONSTRAINT = Columns.TRANSACTION + "=?";

    public final static class Columns {
        private Columns() {}
        public static final String ID = BaseColumns._ID;
        public static final String TRANSACTION = "xact";
        public static final String TIMESTAMP = "timestamp";
        public static final String USER = "user";
        public static final String STATUS = "status";
        public static final String MAX_TIMESTAMP = "max_timestamp";
    }
}
