package com.marakana.android.yamba.svc;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


/**
 * YambaContract
 *
 * The public contract for the Yamba service
 */
public final class YambaContract {
    private YambaContract() {}

    /* The Yamba Service */
    public static final String YAMBA_SERVICE = "com.marakana.yamba.content.POLL";

    /* Permissions
    <service
        android:name=".svc.YambaService"
        android:permission="com.marakana.yamba.content.POLL" >
        <intent-filter>
            <action android:name="com.marakana.yamba.content.POLL" />
        </intent-filter>
    </service>
    */
    /*
    <permission
        android:name="com.marakana.yamba.content.POLL"
        android:protectionLevel="dangerous" />
     */
    public static final String PERM_POLL = "com.marakana.yamba.content.POLL";

    /* Intent parameters */
    public static final String SVC_PARAM_OP = "YambaService.OP";
    public static final int SVC_OP_POLLING_ON = -9901;
    public static final int SVC_OP_POLLING_OFF = -9902;

    /** The Yamba Content Provider */
    public static final String AUTHORITY = "com.marakana.yamba.content";

    /* Permissions
    <provider
        android:name=".data.YambaProvider"
        android:authorities="com.marakana.yamba"
        android:readPermission="com.marakana.yamba.content.PERM_READ"
        android:writePermission="com.marakana.yamba.content.PERM_WRITE" />
    */
    /*
    <permission
        android:name="com.marakana.yamba.content.PERM_READ"
        android:protectionLevel="normal" />
     */
    public static final String PERM_READ = "com.marakana.yamba.content.PERM_READ";

    /*
    <permission
        android:name="com.marakana.yamba.content.PERM_WRITE"
        android:protectionLevel="dangerous" />
     */
    public static final String PERM_WRITE = "com.marakana.yamba.content.PERM_WRITE";

    /** Our base URI */
    public static final Uri BASE_URI = new Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(AUTHORITY)
        .build();

    /** */
    public static final class Posts {
        /** Our table */
        public static final String TABLE = "Posts";

        /** Our base URI */
        public static final Uri URI = BASE_URI.buildUpon().appendPath(TABLE).build();

        /** MIME sub-type for post content */
        public static final String MIME_SUBTYPE = "/vnd.com.marakana.yamba.posts";
        /** Timeline table DIR type */
        public static final String DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + MIME_SUBTYPE;
        /** Timeline table ITEM type */
        public static final String ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + MIME_SUBTYPE;

        /**
         * Column definitions for status information.
         */
        public final static class Columns {
            // Prevent instantiation
            private Columns() {}

            /** Required by ListView */
            public static final String ID = BaseColumns._ID;
            /** Actual post time or null */
            public static final String TIMESTAMP = "timestamp";
            /** Message */
            public static final String STATUS = "status";
        }
    }

    /** */
    public static final class Timeline {
        /** Our table */
        public static final String TABLE = "timeline";

        /** Our base URI */
        public static final Uri URI = BASE_URI.buildUpon().appendPath(TABLE).build();

        /** MIME sub-type for timeline content */
        public static final String MIME_SUBTYPE = "/vnd.com.marakana.yamba.timeline";
        /** Timeline table DIR type */
        public static final String DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + MIME_SUBTYPE;
        /** Timeline table ITEM type */
        public static final String ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + MIME_SUBTYPE;

        /**
         * Column definitions for status information.
         */
        public final static class Columns {
            // Prevent instantiation
            private Columns() {}

            /** Required by ListView */
            public static final String ID = BaseColumns._ID;
            /** Actual post time */
            public static final String TIMESTAMP = "timestamp";
            /** User making the post */
            public static final String USER = "user";
            /** Message */
            public static final String STATUS = "status";
        }
    }
}
