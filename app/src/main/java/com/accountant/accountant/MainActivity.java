package com.accountant.accountant;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import com.accountant.accountant.db.Database;

public class MainActivity extends AppCompatActivity {
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new Database(this);

        setContentView(R.layout.activity_main);

        Fragment inputFragment = new InputFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content, inputFragment)
                .commit();
    }

    Database getDatabase() {
        return db;
    }

    void switchToData() {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content, new DataListFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
