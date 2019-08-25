package com.accountant.accountant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.accountant.accountant.db.Database;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_LOCATION_REQ_CODE = 35415;
    private Database db;

    private FragmentManager fragmentManager;

    private Fragment fragmentMainContent, fragmentTags, fragmentLocations;

    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        fragmentManager = getSupportFragmentManager();
        fragmentMainContent = new MainContentFragment();
        fragmentLocations = new LocationManagementFragment();
        fragmentTags = new TagManagementFragment();

        fragmentManager.beginTransaction()
                .add(R.id.root, fragmentMainContent)
                .commit();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationProvider = new LocationProvider(this, locationManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manageTags:
                switchToManageTags();
                return true;

            case R.id.manageLocations:
                switchToManageLocations();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    Database getDatabase() {
        return db;
    }

    LocationProvider getLocationProvider() {
        return locationProvider;
    }

    void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_LOCATION_REQ_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_LOCATION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationProvider.retryPermissionGranted();
            }
        }
    }

    private void switchToManageTags() {
        fragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.root, fragmentTags)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void switchToManageLocations() {
        fragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.root, fragmentLocations)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
