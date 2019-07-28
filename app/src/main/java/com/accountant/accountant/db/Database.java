package com.accountant.accountant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

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

    public void close() {
        helper.close();
    }
}
