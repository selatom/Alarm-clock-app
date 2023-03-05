package com.example.alarmlist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Create database class. only {@link AppProvider} can use this class
 */
public class AppDatabase extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "com.example.alarmlist.db";
    private static final int VERSION = 1;

    private static AppDatabase instance = null;

    public AppDatabase(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * get an instance of the singleton class
     * @param context
     * @return SQLite database helper object
     */
    static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabase(context);
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql;

        sql = "CREATE TABLE " + ContractAlarm.TABLE_NAME + "("
                + ContractAlarm.Columns._ID + " INTEGER PRIMARY KEY NOT NULL, "
                + ContractAlarm.Columns.NAME + " TEXT,"
                + ContractAlarm.Columns.DATE + " TEXT, "
                + ContractAlarm.Columns.DAY + " TEXT, "
                + ContractAlarm.Columns.HOUR + " TEXT, "
                + ContractAlarm.Columns.STATUS + " TEXT, "
                + ContractAlarm.Columns.FIXED + " TEXT);";

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
