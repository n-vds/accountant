package com.accountant.accountant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Database {
    private DbHelper helper;

    public Database(Context ctx) {
        this.helper = new DbHelper(ctx);
    }

    /**
     * Inserts the specific spending into the database with the current date
     *
     * @param spending the amount spent
     */
    public void insert(int spending) {
        SQLiteDatabase db = helper.getWritableDatabase();

        long now = System.currentTimeMillis();

        ContentValues values = new ContentValues(1);
        values.put(SpendingEntry.COLUMN_AMOUNT, spending);
        values.put(SpendingEntry.COLUMN_DATE, now);
        db.insert(SpendingEntry.TABLE_NAME, null, values);
    }

    public float calcSpendThisMonth() {
        return 0;
    }

    public float calcSpendLast30Days() {
        return 0;
    }

    public Cursor queryData() {
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] columns = new String[]{
                SpendingEntry.COLUMN_ID + " AS _id",
                SpendingEntry.COLUMN_AMOUNT, SpendingEntry.COLUMN_DATE
        };

        return db.query(SpendingEntry.TABLE_NAME, columns,
                null, null, // selection
                null, // group by
                null, // having
                SpendingEntry.COLUMN_DATE + " DESC");
    }

    public void close() {
        helper.close();
    }
}
