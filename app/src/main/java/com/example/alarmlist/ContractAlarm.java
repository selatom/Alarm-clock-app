package com.example.alarmlist;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class ContractAlarm {
    static final String TABLE_NAME = "AlarmCloakList";

    public static class Columns {
        public static final String _ID = BaseColumns._ID;
        public static final String NAME = "Name";
        public static final String DATE = "Date";
        public static final String DAY = "Day";
        public static final String HOUR = "Hour";
        public static final String STATUS = "AlarmStatus";
        public static final String FIXED = "Fixed";
    }

    public static final Uri CONTENT_URI = Uri.withAppendedPath(AppProvider.CONTENT_AUTHORITY_URI, TABLE_NAME);

    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AppProvider.CONTENT_AUTHORITY + "." + TABLE_NAME;

    public static Uri buildAlarmUri (long id) {
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static long getAlarmId(Uri uri) {
        return  ContentUris.parseId(uri);
    }
}
