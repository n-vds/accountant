package com.accountant.accountant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.telecom.Call;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
    public void insert(int spending, Long tag) {
        SQLiteDatabase db = helper.getWritableDatabase();

        long now = Calendar.getInstance().getTimeInMillis();
        int month = Calendar.getInstance().get(Calendar.MONTH);
        int year = Calendar.getInstance().get(Calendar.YEAR);

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues(1);
            values.put(SpendingEntry.AMOUNT, spending);
            values.put(SpendingEntry.DATE, now);
            values.put(SpendingEntry.MONTH, month);
            values.put(SpendingEntry.YEAR, year);
            values.put(SpendingEntry.TAG, tag);
            db.insert(SpendingEntry.TABLE_NAME, null, values);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteEntry(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM " + SpendingEntry.TABLE_NAME +
                " WHERE " + SpendingEntry.ID + " = ?", new Object[]{id});
    }

    public StatisticsEntity queryStatistics() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Calendar calendar = Calendar.getInstance();
        long now = calendar.getTimeInMillis();
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        long thirtyDaysAgo = calendar.getTimeInMillis();

        String sqlBetween = "SELECT " +
                "SUM(" + SpendingEntry.AMOUNT + ") " +
                "FROM " + SpendingEntry.TABLE_NAME + " " +
                "WHERE " + SpendingEntry.DATE + " >= ? AND " +
                SpendingEntry.DATE + " <= ?";


        db.beginTransaction();
        try {
            Cursor c = db.rawQuery(sqlBetween, new String[]{String.valueOf(thirtyDaysAgo), String.valueOf(now)});
            c.moveToFirst();
            int spentLastThirtyDays = c.getInt(0);
            c.close();

            String sqlThisMonth = "SELECT " +
                    "SUM(" + SpendingEntry.AMOUNT + ") " +
                    "FROM " + SpendingEntry.TABLE_NAME + " " +
                    "WHERE " + SpendingEntry.MONTH + " = ? AND " +
                    SpendingEntry.YEAR + " = ?";
            c = db.rawQuery(sqlThisMonth, new String[]{String.valueOf(month), String.valueOf(year)});
            c.moveToFirst();
            int spentThisMonth = c.getInt(0);
            c.close();

            String avgMonth = "SELECT AVG(sum) AS avg, " +
                    SpendingEntry.MONTH + " FROM (" +
                    "SELECT " +
                    "SUM(" + SpendingEntry.AMOUNT + ") AS sum, " +
                    SpendingEntry.MONTH + ", " +
                    SpendingEntry.YEAR + " " +
                    "FROM " + SpendingEntry.TABLE_NAME + " " +
                    "GROUP BY " + SpendingEntry.MONTH + ", " + SpendingEntry.YEAR +
                    ") GROUP BY " + SpendingEntry.MONTH;
            c = db.rawQuery(avgMonth, null);

            float[] avgValues = new float[12];
            Arrays.fill(avgValues, Float.NaN);

            int idxMonth = c.getColumnIndex(SpendingEntry.MONTH);
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

            int totalSpent = (int) db
                    .compileStatement("SELECT SUM(" + SpendingEntry.AMOUNT + ") FROM " + SpendingEntry.TABLE_NAME)
                    .simpleQueryForLong();


            String sqlByTag = "SELECT " +
                    "SUM(" + SpendingEntry.AMOUNT + " ) AS sum, " +
                    SpendingEntry.TAG + ", " +
                    TagEntry.NAME + " " +
                    "FROM " + SpendingEntry.TABLE_NAME +
                    " LEFT JOIN " + TagEntry.TABLE_NAME +
                    " ON " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.TAG +
                    " = " + TagEntry.TABLE_NAME + "." + TagEntry.ID +
                    " GROUP BY " + SpendingEntry.TAG;

            c = db.rawQuery(sqlByTag, null);
            List<StatisticsEntity.SpendingByTagEntry> byTagList = new ArrayList<>(c.getCount());
            int columnSum = c.getColumnIndex("sum");
            int columnTag = c.getColumnIndex(SpendingEntry.TAG);
            int columnName = c.getColumnIndex(TagEntry.NAME);

            if (c.getCount() > 0) {
                c.moveToFirst();

                for (int i = 0; i < c.getCount(); i++) {
                    int sum = c.getInt(columnSum);
                    long tag = c.getLong(columnTag);
                    String name = c.getString(columnName);
                    byTagList.add(new StatisticsEntity.SpendingByTagEntry(tag, name, sum, (float) sum / (float) totalSpent));
                    c.moveToNext();
                }
            }

            c.close();

            db.setTransactionSuccessful();

            return new StatisticsEntity(spentThisMonth, spentLastThirtyDays, avgInOneMonth, avgValues, byTagList);
        } finally {
            db.endTransaction();
        }

    }

    public Cursor queryDataForUserView() {
        SQLiteDatabase db = helper.getReadableDatabase();

        String sql = "SELECT " +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.ID + " AS _id," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.AMOUNT + "," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.DATE + "," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.TAG + "," +
                TagEntry.TABLE_NAME + "." + TagEntry.NAME +
                " FROM " + SpendingEntry.TABLE_NAME +
                " LEFT JOIN " + TagEntry.TABLE_NAME +
                " ON " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.TAG +
                " = " + TagEntry.TABLE_NAME + "." + TagEntry.ID +
                " ORDER BY " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.DATE + " DESC";

        return db.rawQuery(sql, null);
    }

    public SpendingEntity querySingle(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.beginTransaction();

        // TODO refactor, very similar to queryDataForUserView
        String sql = "SELECT " +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.ID + " AS _id," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.AMOUNT + "," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.DATE + "," +
                SpendingEntry.TABLE_NAME + "." + SpendingEntry.TAG + "," +
                TagEntry.TABLE_NAME + "." + TagEntry.NAME +
                " FROM " + SpendingEntry.TABLE_NAME +
                " LEFT JOIN " + TagEntry.TABLE_NAME +
                " ON " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.TAG +
                " = " + TagEntry.TABLE_NAME + "." + TagEntry.ID +
                " WHERE " + SpendingEntry.TABLE_NAME + "." + SpendingEntry.ID + " = ?";

        try (Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id)})) {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();

            long timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(SpendingEntry.DATE));

            int amount = cursor.getInt(cursor.getColumnIndexOrThrow(SpendingEntry.AMOUNT));

            int columnTag = cursor.getColumnIndex(SpendingEntry.TAG);
            Long tagId = cursor.isNull(columnTag) ? null :
                    cursor.getLong(columnTag);

            String tagName = cursor.getString(cursor.getColumnIndex(TagEntry.NAME));

            return new SpendingEntity(id, timestamp, amount, tagId, tagName);
        } finally {
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }


    public Cursor queryAllTagNames() {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery("SELECT " + TagEntry.NAME + ", " +
                TagEntry.ID + " AS _id, " +
                TagEntry.ID +
                " FROM " + TagEntry.TABLE_NAME +
                " ORDER BY " + TagEntry.ID, null);
    }

    public TagList queryTagList() {
        SQLiteDatabase db = helper.getReadableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT " + TagEntry.NAME + ", " +
                TagEntry.ID + " AS _id, " +
                TagEntry.ID +
                " FROM " + TagEntry.TABLE_NAME +
                " ORDER BY " + TagEntry.ID, null)) {

            if (cursor.getCount() == 0) {
                return new TagList();
            }

            cursor.moveToFirst();
            int columnId = cursor.getColumnIndexOrThrow(TagEntry.ID);
            int columnName = cursor.getColumnIndexOrThrow(TagEntry.NAME);

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
    }

    public long insertTag(String tagName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteStatement stmt = db.compileStatement("INSERT INTO " + TagEntry.TABLE_NAME +
                " (" + TagEntry.NAME + ") VALUES (?)");
        stmt.bindString(1, tagName);
        return stmt.executeInsert();
    }

    public void editTagName(long id, String newTagName) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("UPDATE " + TagEntry.TABLE_NAME +
                " SET " + TagEntry.NAME + " = ?" +
                " WHERE " + TagEntry.ID + " = ?", new Object[]{newTagName, id});
    }

    public void deleteTag(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            db.execSQL("UPDATE " + SpendingEntry.TABLE_NAME +
                    " SET " + SpendingEntry.TAG + " = NULL " +
                    " WHERE " + SpendingEntry.TAG + " = ?", new Object[]{id});
            db.execSQL("UPDATE " + LocationEntry.TABLE_NAME +
                    " SET " + LocationEntry.TAG + " = NULL " +
                    " WHERE " + LocationEntry.TAG + " = ?", new Object[]{id});
            db.execSQL("DELETE FROM " + TagEntry.TABLE_NAME +
                    " WHERE " + TagEntry.ID + " = ?", new Object[]{id});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public Cursor queryAllLocations() {
        SQLiteDatabase db = helper.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + LocationEntry.ID + " AS _id, " +
                        LocationEntry.DESC + ", " +
                        LocationEntry.LAT + ", " +
                        LocationEntry.LON + ", " +
                        LocationEntry.TAG + ", " +
                        TagEntry.NAME +
                        " FROM " + LocationEntry.TABLE_NAME +
                        " LEFT OUTER JOIN " + TagEntry.TABLE_NAME +
                        " ON " + LocationEntry.TABLE_NAME + "." + LocationEntry.TAG +
                        " = " + TagEntry.TABLE_NAME + "." + TagEntry.ID +
                        " ORDER BY " + LocationEntry.ID + " DESC", null);
    }

    public LocationEntity queryLocation(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("" +
                "SELECT " + LocationEntry.TABLE_NAME + "." + LocationEntry.ID + " AS _id, " +
                LocationEntry.DESC + ", " +
                LocationEntry.LAT + ", " +
                LocationEntry.LON + ", " +
                LocationEntry.TAG + ", " +
                TagEntry.NAME +
                " FROM " + LocationEntry.TABLE_NAME +
                " LEFT JOIN " + TagEntry.TABLE_NAME +
                " ON " + LocationEntry.TABLE_NAME + "." + LocationEntry.TAG +
                " = " + TagEntry.TABLE_NAME + "." + TagEntry.ID +
                " WHERE " + LocationEntry.TABLE_NAME + "." + LocationEntry.ID + " = ?", new String[]{String.valueOf(id)});

        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();

        Long tag = null;
        String tagName = null;
        if (!c.isNull(c.getColumnIndex(LocationEntry.TAG))) {
            tag = c.getLong(c.getColumnIndex(LocationEntry.TAG));
            tagName = c.getString(c.getColumnIndex(TagEntry.NAME));
        }

        LocationEntity entity = new LocationEntity(
                c.getString(c.getColumnIndex(LocationEntry.DESC)),
                c.getDouble(c.getColumnIndex(LocationEntry.LAT)),
                c.getDouble(c.getColumnIndex(LocationEntry.LON)),
                tag, tagName);

        return entity;
    }

    public void deleteLocation(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("DELETE FROM " + LocationEntry.TABLE_NAME +
                " WHERE " + LocationEntry.ID + " = ?", new Object[]{id});
    }

    public DistanceLocationEntity resolveLocation(double lat, double lon) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("" +
                "SELECT " + LocationEntry.TABLE_NAME + "." + LocationEntry.ID + " AS _id, " +
                LocationEntry.DESC + ", " +
                LocationEntry.LAT + ", " +
                LocationEntry.LON + ", " +
                LocationEntry.TAG + ", " +
                TagEntry.NAME + ", " +
                "(abs(" + LocationEntry.LAT + " - ?) * abs(" + LocationEntry.LON + " - ?)) AS distanceSq" +
                " FROM " + LocationEntry.TABLE_NAME +
                " LEFT JOIN " + TagEntry.TABLE_NAME +
                " ON " + LocationEntry.TABLE_NAME + "." + LocationEntry.TAG +
                " = " + TagEntry.TABLE_NAME + "." + TagEntry.ID +
                " ORDER BY distanceSq ASC", new String[]{
                String.valueOf(lat), String.valueOf(lon)
        });

        if (c.getCount() == 0) {
            return null;
        }
        c.moveToFirst();

        Long tag = null;
        String tagName = null;
        if (!c.isNull(c.getColumnIndex(LocationEntry.TAG))) {
            tag = c.getLong(c.getColumnIndex(LocationEntry.TAG));
            tagName = c.getString(c.getColumnIndex(TagEntry.NAME));
        }
        LocationEntity entity = new LocationEntity(
                c.getString(c.getColumnIndex(LocationEntry.DESC)),
                c.getDouble(c.getColumnIndex(LocationEntry.LAT)),
                c.getDouble(c.getColumnIndex(LocationEntry.LON)),
                tag, tagName);

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

    public void insertLocation(String desc, double lat, double lon, Long tag) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "INSERT INTO " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry.DESC + ", " +
                LocationEntry.LAT + ", " +
                LocationEntry.LON + ", " +
                LocationEntry.TAG + ")" +
                " VALUES (?, ?, ?, ?)";

        SQLiteStatement stm = db.compileStatement(sql);
        stm.bindString(1, desc);
        stm.bindDouble(2, lat);
        stm.bindDouble(3, lon);
        if (tag == null) {
            stm.bindNull(4);
        } else {
            stm.bindLong(4, tag);
        }
        stm.executeInsert();
    }

    public void updateLocation(long id, String desc, double lat, double lon, Long tag) {
        SQLiteDatabase db = helper.getWritableDatabase();
        String sql = "UPDATE " + LocationEntry.TABLE_NAME + " SET " +
                LocationEntry.DESC + " = ?, " +
                LocationEntry.LAT + " = ?, " +
                LocationEntry.LON + " = ?, " +
                LocationEntry.TAG + " = ?" +
                " WHERE " + LocationEntry.ID + " = ?";

        SQLiteStatement stm = db.compileStatement(sql);
        stm.bindString(1, desc);
        stm.bindDouble(2, lat);
        stm.bindDouble(3, lon);
        if (tag == null) {
            stm.bindNull(4);
        } else {
            stm.bindLong(4, tag);
        }
        stm.bindLong(5, id);
        stm.executeUpdateDelete();
    }


    public void updateEntry(long id, long date, int amount, Long tagId) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String sqlSpending = "UPDATE " + SpendingEntry.TABLE_NAME + " SET " +
                    SpendingEntry.DATE + " = ?, " +
                    SpendingEntry.AMOUNT + " = ?, " +
                    SpendingEntry.MONTH + " = ?, " +
                    SpendingEntry.YEAR + " = ?, " +
                    SpendingEntry.TAG + " = ? " +
                    " WHERE " + SpendingEntry.ID + " = ?";
            db.execSQL(sqlSpending, new Object[]{date, amount, month, year, tagId, id});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void close() {
        helper.close();
    }
}
