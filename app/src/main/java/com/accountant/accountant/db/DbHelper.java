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
                SpendingEntry.ID + " INTEGER PRIMARY KEY, " +
                SpendingEntry.AMOUNT + " INTEGER NOT NULL, " +
                SpendingEntry.DATE + " INTEGER NOT NULL, " +
                SpendingEntry.MONTH + " INTEGER NOT NULL," +
                SpendingEntry.YEAR + " INTEGER NOT NULL," +
                SpendingEntry.TAG + " INTEGER REFERENCES " + TagEntry.TABLE_NAME + "(" + TagEntry.ID + "))");

        db.execSQL("CREATE TABLE " + TagEntry.TABLE_NAME + "(" +
                TagEntry.ID + " INTEGER PRIMARY KEY, " +
                TagEntry.NAME + " TEXT NOT NULL, " +
                TagEntry.GROUP + " INTEGER REFERENCES " + TagGroupEntry.TABLE_NAME + "(" + TagGroupEntry.ID + "))");

        db.execSQL("CREATE TABLE " + TagGroupEntry.TABLE_NAME + "(" +
                TagGroupEntry.ID + " INTEGER PRIMARY KEY," +
                TagGroupEntry.NAME + " TEXT NOT NULL," +
                TagGroupEntry.LEVEL + " INTEGER NOT NULL," +
                TagGroupEntry.PARENT + " INTEGER REFERENCES " + TagGroupEntry.TABLE_NAME + "(" + TagGroupEntry.ID + "))");

        db.execSQL("CREATE TABLE " + LocationEntry.TABLE_NAME + "(" +
                LocationEntry.ID + " INTEGER PRIMARY KEY, " +
                LocationEntry.DESC + " TEXT NOT NULL, " +
                LocationEntry.LAT + " REAL NOT NULL, " +
                LocationEntry.LON + " REAL NOT NULL," +
                LocationEntry.TAG + " INTEGER REFERENCES " + TagEntry.TABLE_NAME + "(" + TagEntry.ID + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new SQLException("Can't upgrade database from version " + oldVersion + " to " + newVersion);
    }
}
