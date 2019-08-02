package com.accountant.accountant.db;

public class LocationEntity {
    public final String desc;
    public final double lat;
    public final double lon;
    public final long tag;

    public LocationEntity(String desc, double lat, double lon, long tag) {
        this.desc = desc;
        this.lat = lat;
        this.lon = lon;
        this.tag = tag;
    }
}
