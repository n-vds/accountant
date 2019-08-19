package com.accountant.accountant.db;

public class DistanceLocationEntity {
    public final LocationEntity location;
    public final double distance;


    public DistanceLocationEntity(LocationEntity location, double distance) {
        this.location = location;
        this.distance = distance;
    }
}
