package com.accountant.accountant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.Collection;

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
        db.beginTransaction();

        // TODO refactor, very similar to queryDataForUserView
        try {
            String sql = "SELECT " + SpendingEntry.COLUMN_AMOUNT + ", " + SpendingEntry.COLUMN_DATE +
                    " FROM " + SpendingEntry.TABLE_NAME +
                    " WHERE " + SpendingEntry.COLUMN_ID + " = ?";

            Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)});
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(SpendingEntry.COLUMN_DATE));
            int amount = cursor.getInt(cursor.getColumnIndexOrThrow(SpendingEntry.COLUMN_AMOUNT));
            cursor.close();

            String sqlTags = "SELECT " + TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_ID + " AS tag_id, " +
                    TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_NAME + " AS tag_name" +
                    " FROM " + TagSpendingEntry.TABLE_NAME +
                    " LEFT JOIN " + TagEntry.TABLE_NAME +
                    " ON " + TagSpendingEntry.TABLE_NAME + "." + TagSpendingEntry.COLUMN_TAG +
                    " = " + TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_ID +
                    " WHERE " + TagSpendingEntry.TABLE_NAME + "." + TagSpendingEntry.COLUMN_SPENDING +
                    " = ?";

            cursor = db.rawQuery(sqlTags, new String[]{String.valueOf(id)});
            cursor.moveToFirst();

            long[] tagIds = new long[cursor.getCount()];
            String[] tagNames = new String[cursor.getCount()];
            for (int i = 0; i < tagIds.length; i++) {
                tagIds[i] = cursor.getLong(cursor.getColumnIndexOrThrow("tag_id"));
                tagNames[i] = cursor.getString(cursor.getColumnIndexOrThrow("tag_name"));
                cursor.moveToNext();
            }
            cursor.close();
            return new SpendingEntity(id, timestamp, amount, tagIds, tagNames);
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }


    public Cursor queryAllTagNames() {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery("SELECT " + TagEntry.COLUMN_NAME + ", " +
                TagEntry.COLUMN_ID + " AS _id, " +
                TagEntry.COLUMN_ID +
                " FROM " + TagEntry.TABLE_NAME +
                " ORDER BY " + TagEntry.COLUMN_ID, null);
    }

    public long insertTag(String tagName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement("INSERT INTO " + TagEntry.TABLE_NAME +
                " (" + TagEntry.COLUMN_NAME + ") VALUES (?)");
        stmt.bindString(1, tagName);
        return stmt.executeInsert();
    }


    public Cursor queryAllLocations() {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + LocationEntry.COLUMN_ID + " AS _id, " +
                        LocationEntry.COLUMN_DESC + ", " +
                        LocationEntry.COLUMN_LAT + ", " +
                        LocationEntry.COLUMN_LON + ", " +
                        LocationEntry.COLUMN_TAG +
                        " FROM " + LocationEntry.TABLE_NAME +
                        " ORDER BY " + LocationEntry.COLUMN_ID, null);
    }

    public void insertLocation(String desc, double lat, double lon, long tag) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "INSERT INTO " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry.COLUMN_DESC + ", " +
                LocationEntry.COLUMN_LAT + ", " +
                LocationEntry.COLUMN_LON + ", " +
                LocationEntry.COLUMN_TAG + ")" +
                " VALUES (?, ?, ?, ?)";

        SQLiteStatement stm = db.compileStatement(sql);
        stm.bindString(1, desc);
        stm.bindDouble(2, lat);
        stm.bindDouble(3, lon);
        stm.bindLong(4, tag);
        stm.executeInsert();
    }


    public void updateEntry(long id, long date, int amount, Collection<Long> tagIds) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String sqlSpending = "UPDATE " + SpendingEntry.TABLE_NAME +
                    " SET " + SpendingEntry.COLUMN_DATE + " = ?, " +
                    SpendingEntry.COLUMN_AMOUNT + " = ? " +
                    " WHERE " + SpendingEntry.COLUMN_ID + " = ?";
            db.execSQL(sqlSpending, new Object[]{date, amount, id});

            String sqlDeleteOldTags = "DELETE FROM " + TagSpendingEntry.TABLE_NAME +
                    " WHERE " + TagSpendingEntry.COLUMN_SPENDING + " = ?";
            db.execSQL(sqlDeleteOldTags, new Object[]{id});

            SQLiteStatement sqlInsertTag = db.compileStatement("INSERT INTO " + TagSpendingEntry.TABLE_NAME +
                    " (" + TagSpendingEntry.COLUMN_SPENDING + ", " + TagSpendingEntry.COLUMN_TAG + ")" +
                    " VALUES (?, ?)");

            for (long tagId : tagIds) {
                // Index is 1 based
                sqlInsertTag.bindLong(1, id);
                sqlInsertTag.bindLong(2, tagId);
                sqlInsertTag.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void close() {
        helper.close();
    }
}
