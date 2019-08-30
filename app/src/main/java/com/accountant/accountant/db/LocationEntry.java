package com.accountant.accountant.db;

public final class LocationEntry {
    private LocationEntry() {
    }

    public static final String TABLE_NAME = "locations";

    public static final String ID = "id";
    public static final String LAT = "lat";
    public static final String LON = "lon";
    public static final String TAG = "tag";
    public static final String DESC = "descr";
}
