package com.accountant.accountant;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.LocationEntry;

public class LocationManagementFragment extends ListFragment {
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Database db = activity.getDatabase();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity,
                R.layout.location_row,
                db.queryAllLocations(),
                new String[]{LocationEntry.COLUMN_DESC, LocationEntry.COLUMN_LAT, LocationEntry.COLUMN_LON, LocationEntry.COLUMN_TAG},
                new int[]{R.id.desc, R.id.lat, R.id.lon, R.id.tags}, 0);

        setListAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_locations, container, false);
        root.findViewById(R.id.add).setOnClickListener((_v) -> onAddClick());
        return root;
    }

    private void onAddClick() {
        EditLocationDialog dialog = new EditLocationDialog();
        Bundle args = new Bundle();
        args.putBoolean("new", true);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "editlocationdialog");
    }

}
