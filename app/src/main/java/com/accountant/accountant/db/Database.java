package com.accountant.accountant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import java.util.Arrays;
import java.util.Calendar;
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
    public void insert(int spending, long... tags) {
        SQLiteDatabase db = helper.getWritableDatabase();

        long now = System.currentTimeMillis();
        int month = Calendar.getInstance().get(Calendar.MONTH);

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues(1);
            values.put(SpendingEntry.COLUMN_AMOUNT, spending);
            values.put(SpendingEntry.COLUMN_DATE, now);
            values.put(SpendingEntry.COLUMN_MONTH, month);
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

    public StatisticsEntity queryStatistics() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        int month = calendar.get(Calendar.MONTH);
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        long thirtyDaysAgo = calendar.getTimeInMillis();

        db.beginTransaction();
        try {
            String sqlBetween = "SELECT " +
                    "SUM(" + SpendingEntry.COLUMN_AMOUNT + ") " +
                    "FROM " + SpendingEntry.TABLE_NAME + " " +
                    "WHERE " + SpendingEntry.COLUMN_DATE + " >= ? AND " +
                    SpendingEntry.COLUMN_DATE + " <= ?";

            Cursor c = db.rawQuery(sqlBetween, new String[]{String.valueOf(thirtyDaysAgo), String.valueOf(now)});
            c.moveToFirst();
            int spentLastThirtyDays = c.getInt(0);
            c.close();

            String sqlThisMonth = "SELECT " +
                    "SUM(" + SpendingEntry.COLUMN_AMOUNT + ") " +
                    "FROM " + SpendingEntry.TABLE_NAME + " " +
                    "WHERE " + SpendingEntry.COLUMN_MONTH + " = ?";
            c = db.rawQuery(sqlThisMonth, new String[]{String.valueOf(month)});
            c.moveToFirst();
            int spentThisMonth = c.getInt(0);
            c.close();

            String avgMonth = "SELECT " +
                    "AVG(" + SpendingEntry.COLUMN_AMOUNT + ") AS avg, " +
                    SpendingEntry.COLUMN_MONTH + " " +
                    "FROM " + SpendingEntry.TABLE_NAME + " " +
                    "GROUP BY " + SpendingEntry.COLUMN_MONTH;
            c = db.rawQuery(avgMonth, null);

            float[] avgValues = new float[12];
            Arrays.fill(avgValues, Float.NaN);

            int idxMonth = c.getColumnIndex(SpendingEntry.COLUMN_MONTH);
            int idxAvg = c.getColumnIndex("avg");

            float sumOfMonthAvg = 0f;
            int count = 0;

            for (int i = 0; i < c.getCount(); i++) {
                if (i == 0) {
                    c.moveToFirst();
                }
                float avg = c.getFloat(idxAvg);
                avgValues[c.getInt(idxMonth)] = avg; // Calendar's month start at 0
                sumOfMonthAvg += avg;
                count++;
                c.moveToNext();
            }

            float avgInOneMonth = sumOfMonthAvg / (float) count;

            db.setTransactionSuccessful();

            return new StatisticsEntity(spentThisMonth, spentLastThirtyDays, avgInOneMonth, avgValues);
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

    public TagList queryTagList() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + TagEntry.COLUMN_NAME + ", " +
                TagEntry.COLUMN_ID + " AS _id, " +
                TagEntry.COLUMN_ID +
                " FROM " + TagEntry.TABLE_NAME +
                " ORDER BY " + TagEntry.COLUMN_ID, null);

        if (cursor.getCount() == 0) {
            return new TagList();
        }

        cursor.moveToFirst();
        int columnId = cursor.getColumnIndexOrThrow(TagEntry.COLUMN_ID);
        int columnName = cursor.getColumnIndexOrThrow(TagEntry.COLUMN_NAME);

        String[] tagNames = new String[cursor.getCount()];
        long[] tagIds = new long[cursor.getCount()];

        for (int i = 0; i < cursor.getCount(); i++) {
            tagNames[i] = cursor.getString(columnName);
            tagIds[i] = cursor.getLong(columnId);
            cursor.moveToNext();
        }

        TagList list = new TagList(tagIds, tagNames);
        return list;
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
                        " ORDER BY " + LocationEntry.COLUMN_ID + " DESC", null);
    }

    public LocationEntity queryLocation(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("" +
                "SELECT " + LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_ID + " AS _id, " +
                LocationEntry.COLUMN_DESC + ", " +
                LocationEntry.COLUMN_LAT + ", " +
                LocationEntry.COLUMN_LON + ", " +
                LocationEntry.COLUMN_TAG + ", " +
                TagEntry.COLUMN_NAME +
                " FROM " + LocationEntry.TABLE_NAME +
                " LEFT JOIN " + TagEntry.TABLE_NAME +
                " ON " + LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_TAG +
                " = " + TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_ID +
                " WHERE " + LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});

        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();

        LocationEntity entity = new LocationEntity(
                c.getString(c.getColumnIndex(LocationEntry.COLUMN_DESC)),
                c.getDouble(c.getColumnIndex(LocationEntry.COLUMN_LAT)),
                c.getDouble(c.getColumnIndex(LocationEntry.COLUMN_LON)),
                c.getLong(c.getColumnIndex(LocationEntry.COLUMN_TAG)),
                c.getString(c.getColumnIndex(TagEntry.COLUMN_NAME)));

        return entity;
    }

    public DistanceLocationEntity resolveLocation(double lat, double lon) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("" +
                "SELECT " + LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_ID + " AS _id, " +
                LocationEntry.COLUMN_DESC + ", " +
                LocationEntry.COLUMN_LAT + ", " +
                LocationEntry.COLUMN_LON + ", " +
                LocationEntry.COLUMN_TAG + ", " +
                TagEntry.COLUMN_NAME + ", " +
                "(abs(" + LocationEntry.COLUMN_LAT + " - ?) * abs(" + LocationEntry.COLUMN_LON + " - ?)) AS distanceSq" +
                " FROM " + LocationEntry.TABLE_NAME +
                " LEFT JOIN " + TagEntry.TABLE_NAME +
                " ON " + LocationEntry.TABLE_NAME + "." + LocationEntry.COLUMN_TAG +
                " = " + TagEntry.TABLE_NAME + "." + TagEntry.COLUMN_ID +
                " ORDER BY distanceSq ASC", new String[]{
                String.valueOf(lat), String.valueOf(lon)
        });

        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();

        LocationEntity entity = new LocationEntity(
                c.getString(c.getColumnIndex(LocationEntry.COLUMN_DESC)),
                c.getDouble(c.getColumnIndex(LocationEntry.COLUMN_LAT)),
                c.getDouble(c.getColumnIndex(LocationEntry.COLUMN_LON)),
                c.getLong(c.getColumnIndex(LocationEntry.COLUMN_TAG)),
                c.getString(c.getColumnIndex(TagEntry.COLUMN_NAME)));

        double distance = coordinateDistance(lat, lon, entity.lat, entity.lon);
        return new DistanceLocationEntity(entity, distance);

    }

    private double coordinateDistance(double lat1, double lon1, double lat2, double lon2) {
        // taken from
        // https://stackoverflow.com/questions/365826/calculate-distance-between-2-gps-coordinates
        // https://stackoverflow.com/a/365853
        // by users cletus (https://stackoverflow.com/users/18393/cletus)
        // and coldfire (https://stackoverflow.com/users/886001/coldfire)
        // licensed under cc by-sa 3.0 with attribution required

        double deltaLat = Math.toRadians(lat1 - lat2);
        double deltaLon = Math.toRadians(lon1 - lon2);

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2) *
                        Math.cos(Math.toRadians(lat1)) *
                        Math.cos(Math.toRadians(lat2));

        double d = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = 6371000 * d; // earth radius in meter

        return distance;
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

    public void updateLocation(long id, String desc, double lat, double lon, long tag) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "UPDATE " + LocationEntry.TABLE_NAME + " SET " +
                LocationEntry.COLUMN_DESC + " = ?, " +
                LocationEntry.COLUMN_LAT + " = ?, " +
                LocationEntry.COLUMN_LON + " = ?, " +
                LocationEntry.COLUMN_TAG + " = ?" +
                " WHERE " + LocationEntry.COLUMN_ID + " = ?";

        SQLiteStatement stm = db.compileStatement(sql);
        stm.bindString(1, desc);
        stm.bindDouble(2, lat);
        stm.bindDouble(3, lon);
        stm.bindLong(4, tag);
        stm.bindLong(5, id);
        stm.executeUpdateDelete();
    }


    public void updateEntry(long id, long date, int amount, Collection<Long> tagIds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        int month = calendar.get(Calendar.MONTH);

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String sqlSpending = "UPDATE " + SpendingEntry.TABLE_NAME +
                    " SET " + SpendingEntry.COLUMN_DATE + " = ?, " +
                    SpendingEntry.COLUMN_AMOUNT + " = ?, " +
                    SpendingEntry.COLUMN_MONTH + " = ? " +
                    " WHERE " + SpendingEntry.COLUMN_ID + " = ?";
            db.execSQL(sqlSpending, new Object[]{date, amount, month, id});

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
