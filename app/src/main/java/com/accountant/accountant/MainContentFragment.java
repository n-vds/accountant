package com.accountant.accountant;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class MainContentFragment extends Fragment {
    private BottomNavigationView navbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        navbar = root.findViewById(R.id.navigation);
        navbar.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
        navbar.setSelectedItemId(R.id.action_insert);

        return root;
    }

    void switchToData() {
        navbar.setSelectedItemId(R.id.action_show_list);
    }

    private void switchDirectlyToData() {
        getFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .replace(R.id.content, new DataListFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    private void switchDirectlyToInsert() {
        getFragmentManager()
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
}
