package com.accountant.accountant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Database {
    public static final String COLUMN_TAG_LIST = "tag_list";

    private DbHelper helper;

    public Database(Context ctx) {
        this.helper = new DbHelper(ctx);
    }

    /**
     * Inserts the specific spending into the database with the current date
     *
     * @param spending the amount spent
     */
    public void insert(int spending, int... tags) {
        SQLiteDatabase db = helper.getWritableDatabase();

        long now = System.currentTimeMillis();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues(1);
            values.put(SpendingEntry.COLUMN_AMOUNT, spending);
            values.put(SpendingEntry.COLUMN_DATE, now);
            long rowId = db.insert(SpendingEntry.TABLE_NAME, null, values);

            if (tags != null && tags.length > 0) {
                StringBuilder tagValuesString = new StringBuilder();

                for (int i = 0; i < tags.length; i++) {
                    if (i != 0) {
                        tagValuesString.append(' ').append(',');
                    }
                    tagValuesString.append('(');
                    tagValuesString.append(rowId);
                    tagValuesString.append(',').append(' ');
                    tagValuesString.append(tags[i]);
                    tagValuesString.append(')');
                }

                db.execSQL("INSERT INTO " + TagSpendingEntry.TABLE_NAME + " (" +
                        TagSpendingEntry.COLUMN_SPENDING + ", " +
                        TagSpendingEntry.COLUMN_TAG + ") " +
                        "VALUES " + tagValuesString);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Cursor queryDataForUserView() {
        SQLiteDatabase db = helper.getReadableDatabase();

        String sql = "SELECT " +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_ID + " AS _id," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_AMOUNT + "," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_DATE + "," +
                "group_concat(" + TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_NAME + ", ', ') AS " + Database.COLUMN_TAG_LIST +
                " FROM " + SpendingEntry.TABLE_NAME +
                " LEFT JOIN " + TagSpendingEntry.TABLE_NAME +
                " ON " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_ID +
                " = " + TagSpendingEntry.TABLE_NAME + "." + TagSpendingEntry.COLUMN_SPENDING +
                " LEFT JOIN " + TagEntry.TABLE_NAME +
                " ON " + TagSpendingEntry.TABLE_NAME + "." + TagSpendingEntry.COLUMN_TAG +
                " = " + TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_ID +
                " GROUP BY " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_ID +
                " ORDER BY " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_DATE + " DESC";

        return db.rawQuery(sql, null);
    }

    public SpendingEntity querySingle(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        // TODO refactor, very similar to queryDataForUserView
        String sql = "SELECT " +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_ID + " AS _id," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_AMOUNT + "," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_DATE + "," +
                TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_ID + " AS tag_id," +
                TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_NAME + " AS tag_name" +
                " FROM " + SpendingEntry.TABLE_NAME +
                " LEFT JOIN " + TagSpendingEntry.TABLE_NAME +
                " ON " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_ID +
                " = " + TagSpendingEntry.TABLE_NAME + "." + TagSpendingEntry.COLUMN_SPENDING +
                " LEFT JOIN " + TagEntry.TABLE_NAME +
                " ON " + TagSpendingEntry.TABLE_NAME + "." + TagSpendingEntry.COLUMN_TAG +
                " = " + TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_ID +
                " WHERE " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_ID + " = " + id +
                " ORDER BY " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_DATE + " DESC, " +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.COLUMN_ID;

        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() == 0) {
            return null;
        }
        cursor.moveToFirst();
        long timestamp = cursor.getLong(cursor.getColumnIndex(SpendingEntry.COLUMN_DATE));
        int amount = cursor.getInt(cursor.getColumnIndex(SpendingEntry.COLUMN_AMOUNT));
        long[] tagIds = new long[cursor.getCount()];
        String[] tagNames = new String[cursor.getCount()];
        for (int i = 0; i < tagIds.length; i++) {
            tagIds[i] = cursor.getLong(cursor.getColumnIndex("tag_id"));
            tagNames[i] =  cursor.getString(cursor.getColumnIndex("tag_name"));
            cursor.moveToNext();
        }
        cursor.close();
        return new SpendingEntity(id, timestamp, amount, tagIds, tagNames);
    }

    public void close() {
        helper.close();
    }
}
