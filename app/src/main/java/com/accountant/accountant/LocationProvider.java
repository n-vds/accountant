package com.accountant.accountant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;

public class LocationProvider {
    private Listener listener;

    private LocationProviderUpdate updateListener = null;

    private Context ctx;
    private LocationManager manager;
    private boolean requestsUpdates = false;

    private Status status = Status.INACTIVE;
    private double lat, lon;

    public LocationProvider(Context ctx, LocationManager manager) {
        this.manager = manager;
        this.ctx = ctx;
        this.listener = new Listener();
    }

    public void retryPermissionGranted() {
        if (updateListener != null) {
            startRequest();
        }
    }

    public void startRequest(LocationProviderUpdate updateListener) {
        this.updateListener = updateListener;
        startRequest();
    }

    private void startRequest() {
        if (updateListener == null) {
            return;
        }

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            status = Status.NO_PERMISSION;
            updateListener.onUpdate(status, 0, 0);
            return;
        }

        if (requestsUpdates) {
            return;
        }
        requestsUpdates = true;

        status = Status.WAITING;
        updateListener.onUpdate(status, 0, 0);


        boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean networkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (gpsEnabled) {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 100, listener);
        } else if (networkEnabled) {
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 100, listener);
        }
    }

    public void stopRequest() {
        if (updateListener != null && requestsUpdates) {
            status = Status.INACTIVE;
            manager.removeUpdates(listener);
        }
        requestsUpdates = false;
        updateListener = null;
    }

    public enum Status {
        INACTIVE, NO_PERMISSION, WAITING, GOT_DATA;
    }

    private class Listener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            status = Status.GOT_DATA;

            if (updateListener != null && requestsUpdates) {
                updateListener.onUpdate(status, lat, lon);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    public interface LocationProviderUpdate {
        void onUpdate(Status status, double lat, double lon);
    }

}
