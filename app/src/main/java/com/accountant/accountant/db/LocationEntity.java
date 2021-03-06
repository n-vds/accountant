package com.accountant.accountant.db;

public class LocationEntity {
    public final String desc;
    public final double lat;
    public final double lon;
    public final Long tag;
    public final String tagName;

    public LocationEntity(String desc, double lat, double lon, Long tag, String tagName) {
        this.desc = desc;
        this.lat = lat;
        this.lon = lon;
        this.tag = tag;
        this.tagName = tagName;
    }
}
