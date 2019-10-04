package com.accountant.accountant.view.locations;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.ListFragment;
import com.accountant.accountant.MainActivity;
import com.accountant.accountant.R;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.LocationEntry;
import com.accountant.accountant.view.DeleteSelectedActionMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LocationManagementFragment extends ListFragment {
    private FloatingActionButton fab;
    private ActionMode selectDeleteActionMode = null;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Database db = activity.getDatabase();

        getListView().setOnItemLongClickListener((_list, _view, position, _l) -> {
            beginActionMode();
            getListView().setItemChecked(position, true);
            return true;
        });

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity,
                R.layout.location_row,
                db.queryAllLocations(),
                new String[]{LocationEntry.DESC, LocationEntry.LAT, LocationEntry.LON, LocationEntry.TAG},
                new int[]{R.id.desc, R.id.lat, R.id.lon, R.id.tags}, 0);

        setListAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_locations, container, false);
        fab = root.findViewById(R.id.add);
        fab.setOnClickListener((_v) -> onAddClick());
        return root;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (selectDeleteActionMode != null) {
            int count = getListView().getCheckedItemCount();
            selectDeleteActionMode.setTitle(count + " selected");
            if (count == 0) {
                selectDeleteActionMode.finish();
            }
            return;
        }

        EditLocationDialog dialog = new EditLocationDialog();
        Bundle args = new Bundle();
        args.putBoolean("new", false);
        args.putLong("id", id);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "editlocationdialog");
    }

    private void onAddClick() {
        EditLocationDialog dialog = new EditLocationDialog();
        Bundle args = new Bundle();
        args.putBoolean("new", true);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "editlocationdialog");
    }

    void notifyDataChanged() {
        CursorAdapter adapter = (CursorAdapter) getListAdapter();
        Database db = ((MainActivity) getActivity()).getDatabase();
        adapter.changeCursor(db.queryAllLocations());
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
    }

    private void beginActionMode() {
        if (selectDeleteActionMode != null) {
            return;
        }
        DeleteSelectedActionMode callback = new DeleteSelectedActionMode(getListView(), this::deleteSelectedLocations);
        callback.setOnDestroyListener(() -> {
            fab.show();
            selectDeleteActionMode = null;
        });
        selectDeleteActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(callback);
        fab.hide();
    }

    private void deleteSelectedLocations() {
        Database db = ((MainActivity) getActivity()).getDatabase();
        for (long id : getListView().getCheckedItemIds()) {
            db.deleteLocation(id);
        }
        notifyDataChanged();
    }
}
