package com.accountant.accountant;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
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
    private BottomNavigationView navbar;

    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        navbar = findViewById(R.id.navigation);
        navbar.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        fragmentManager = getSupportFragmentManager();
        navbar.setSelectedItemId(R.id.action_insert);

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
        if (item.getItemId() == R.id.manageTags) {
            switchToManageTags();
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

    void switchToManageTags() {
        // TODO: this does not uncheck the navbar, leading to a bug:
        // TODO: if the user clicks on the checked item, this fragment is not closed
        fragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content, new TagManagementFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    void switchToData() {
        navbar.setSelectedItemId(R.id.action_show_list);
    }

    private void switchDirectlyToData() {
        fragmentManager
                .beginTransaction()
                .replace(R.id.content, new DataListFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void switchDirectlyToInsert() {
        fragmentManager
                .beginTransaction()
                .replace(R.id.content, new InputFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == navbar.getSelectedItemId()) {
            // FIXME this only works as long as theres no deeper navigation
            return false;
        }

        switch (item.getItemId()) {
            case R.id.action_home:
                break;
            case R.id.action_insert:
                switchDirectlyToInsert();
                break;
            case R.id.action_show_list:
                switchDirectlyToData();
                break;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
