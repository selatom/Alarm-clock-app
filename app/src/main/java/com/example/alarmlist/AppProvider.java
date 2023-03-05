package com.example.alarmlist;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Provider for the alarm clock app. this the only class who know about {@link AppDatabase}
 */
public class AppProvider extends ContentProvider {
    private AppDatabase mOpenHelper;

    public static final UriMatcher sUriMatcher = buildUriMatcher();

    public static final String CONTENT_AUTHORITY = "com.example.alarmlist.provider";
    public static final Uri CONTENT_AUTHORITY_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final int ALARMS = 100;
    private static final int ALARMS_ID = 101;


    private static UriMatcher buildUriMatcher () {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(CONTENT_AUTHORITY, ContractAlarm.TABLE_NAME, ALARMS);
        matcher.addURI(CONTENT_AUTHORITY, ContractAlarm.TABLE_NAME + " /#", ALARMS_ID);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = AppDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final int match = sUriMatcher.match(uri);

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (match) {
            case ALARMS:
                queryBuilder.setTables(ContractAlarm.TABLE_NAME);
                break;
            case ALARMS_ID:
                queryBuilder.setTables(ContractAlarm.TABLE_NAME);
                long id = ContractAlarm.getAlarmId(uri);
                queryBuilder.appendWhere(ContractAlarm.Columns._ID + " = " + id);
            default:
                throw new IllegalArgumentException("Unknown uri " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        if (match == ALARMS)
            return ContractAlarm.CONTENT_TYPE;
        else
            throw new IllegalArgumentException("unknown uri " + uri);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);

        final SQLiteDatabase db;

        long recordId;
        Uri returnUri;

        if (match == ALARMS) {
            db = mOpenHelper.getReadableDatabase();
            recordId = db.insert(ContractAlarm.TABLE_NAME, null, values);

            if (recordId >= 0) {
                returnUri = ContractAlarm.buildAlarmUri(recordId);
            } else {
                throw new IllegalArgumentException("Failed to insert into " + uri);
            }
        } else {
            throw new IllegalArgumentException("Unknown uri" + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        SQLiteDatabase db;

        int count;
        String selectionCriteria;

        switch (match) {
            case ALARMS:
                db = mOpenHelper.getReadableDatabase();
                count = db.delete(ContractAlarm.TABLE_NAME, selection, selectionArgs);
                break;
            case ALARMS_ID:
                db= mOpenHelper.getReadableDatabase();
                long id = ContractAlarm.getAlarmId(uri);
                selectionCriteria = ContractAlarm.Columns._ID + " = " + id;
                if ((selection != null) && (selection.length() > 0)) {
                    selectionCriteria = selectionCriteria + " AND (" + selection + ")";
                }
                count = db.delete(ContractAlarm.TABLE_NAME, selectionCriteria, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri);
        }

        if (count > 0) {
            //Notify about the changes
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        SQLiteDatabase db;

        int count;
        String selectionCriteria;

        switch (match) {
            case ALARMS:
                db = mOpenHelper.getReadableDatabase();
                count = db.update(ContractAlarm.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ALARMS_ID:
                db= mOpenHelper.getReadableDatabase();
                long id = ContractAlarm.getAlarmId(uri);
                selectionCriteria = ContractAlarm.Columns._ID + " = " + id;
                if ((selection != null) && (selection.length() > 0)) {
                    selectionCriteria = selectionCriteria + " AND (" + selection + ")";
                }
                count = db.update(ContractAlarm.TABLE_NAME, values, selectionCriteria, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri);
        }

        if (count > 0) {
            //Notify about the changes
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }
}
