package com.twitter.university.android.yamba.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public class YambaContract {
    private YambaContract() { }

    public static final int VERSION = 1;

    /** Yamba Service */
    public static class Service {
        private Service() {}

        // action: poll once.  No parameters.
        public static final String ACTION_SYNC
            = "com.twitter.university.android.yamba.service.SYNC";
    }

    public static final String AUTHORITY = "com.twitter.university.android.yamba";

    public static final Uri BASE_URI = new Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(AUTHORITY)
        .build();

    public static class Posts {
        private Posts() { }

        public static final String TABLE = "posts";

        public static final Uri URI = BASE_URI.buildUpon().appendPath(TABLE).build();

        private static final String MINOR_TYPE = "/vnd." + AUTHORITY + "." + TABLE;

        public static final String ITEM_TYPE
            = ContentResolver.CURSOR_ITEM_BASE_TYPE + MINOR_TYPE;
        public static final String DIR_TYPE
            = ContentResolver.CURSOR_DIR_BASE_TYPE + MINOR_TYPE;

        public static class Columns {
            public static final String ID = BaseColumns._ID;
            public static final String TIMESTAMP = "timestamp";
            public static final String TRANSACTION = "xact";
            public static final String SENT = "sent";
            public static final String TWEET = "tweet";
        }
    }

    public static class Timeline {
        private Timeline() { }

        public static final String TABLE = "timeline";

        public static final Uri URI = BASE_URI.buildUpon().appendPath(TABLE).build();

        private static final String MINOR_TYPE = "/vnd." + AUTHORITY + "." + TABLE;

        public static final String ITEM_TYPE
            = ContentResolver.CURSOR_ITEM_BASE_TYPE + MINOR_TYPE;
        public static final String DIR_TYPE
            = ContentResolver.CURSOR_DIR_BASE_TYPE + MINOR_TYPE;

        public static class Columns {
            public static final String ID = BaseColumns._ID;
            public static final String HANDLE = "handle";
            public static final String TWEET = "tweet";
            public static final String TIMESTAMP = "timestamp";
        }
    }
}
