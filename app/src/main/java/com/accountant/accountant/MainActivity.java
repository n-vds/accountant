package com.accountant.accountant;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.accountant.accountant.db.Database;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_LOCATION_REQ_CODE = 35415;

    private Database db;
    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

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
        NavController nav = Navigation.findNavController(this, R.id.nav_host_fragment);
        switch (nav.getCurrentDestination().getId()) {
            case R.id.tagManagementFragment:
            case R.id.locationManagementFragment:
                nav.popBackStack(R.id.mainContentFragment, false);
        }
        nav.navigate(R.id.mainContentToTagManagement);
    }

    private void switchToManageLocations() {
        NavController nav = Navigation.findNavController(this, R.id.nav_host_fragment);
        switch (nav.getCurrentDestination().getId()) {
            case R.id.tagManagementFragment:
            case R.id.locationManagementFragment:
                nav.popBackStack(R.id.mainContentFragment, false);
        }

        nav.navigate(R.id.mainContentToLocationManagement);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
