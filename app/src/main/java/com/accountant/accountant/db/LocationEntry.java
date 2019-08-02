package com.accountant.accountant.db;

public final class LocationEntry {
    private LocationEntry() {
    }

    public static final String TABLE_NAME = "locations";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LON = "lon";
    public static final String COLUMN_TAG = "tag";
}
