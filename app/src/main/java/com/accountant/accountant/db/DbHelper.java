package com.accountant.accountant.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "spending.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + SpendingEntry.TABLE_NAME + " ( " +
                SpendingEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                SpendingEntry.COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                SpendingEntry.COLUMN_DATE + " INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE " + TagEntry.TABLE_NAME + "(" +
                TagEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                TagEntry.COLUMN_NAME + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + LocationEntry.TABLE_NAME + "(" +
                LocationEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                LocationEntry.COLUMN_DESC + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_LAT + " REAL NOT NULL, " +
                LocationEntry.COLUMN_LON + " REAL NOT NULL," +
                LocationEntry.COLUMN_TAG + " INTEGER NOT NULL REFERENCES " + TagEntry.TABLE_NAME + "(" + TagEntry.COLUMN_ID + "))");

        db.execSQL("CREATE TABLE " + TagSpendingEntry.TABLE_NAME + "(" +
                TagSpendingEntry.COLUMN_ID + " INTEGER PRIMARY KEY, " +
                TagSpendingEntry.COLUMN_SPENDING + " INTEGER NOT NULL REFERENCES " +
                SpendingEntry.TABLE_NAME + "(" + SpendingEntry.COLUMN_ID + ")," +
                TagSpendingEntry.COLUMN_TAG + " INTEGER NOT NULL REFERENCES " +
                TagEntry.TABLE_NAME + "(" + TagEntry.COLUMN_ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLException("Can't upgrade database from version " + oldVersion + " to " + newVersion);
    }
}
