package com.accountant.accountant;


import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.accountant.accountant.db.Database;

public class MainActivity extends AppCompatActivity {
    private Database db;

    private FragmentManager fragmentManager;
    private BottomNavigationView navbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this);

        setContentView(R.layout.activity_main);

        navbar = findViewById(R.id.navigation);
        navbar.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);

        fragmentManager = getSupportFragmentManager();
        navbar.setSelectedItemId(R.id.action_insert);
    }

    Database getDatabase() {
        return db;
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
