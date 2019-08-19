package com.accountant.accountant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.LocationEntity;

public class EditLocationDialog extends DialogFragment {
    private LocationProvider locationProvider;
    private LocationProvider.LocationProviderUpdate locationListener;

    private EditText vDesc;
    private EditText vLat;
    private EditText vLon;
    private Button vSetHere;
    private TextView vListTags;
    private Button vEditTags;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        locationProvider = ((MainActivity) getActivity()).getLocationProvider();

        View root = requireActivity().getLayoutInflater()
                .inflate(R.layout.dialog_editlocation, null);

        vDesc = root.findViewById(R.id.desc);
        vLat = root.findViewById(R.id.lat);
        vLon = root.findViewById(R.id.lon);
        vSetHere = root.findViewById(R.id.setHere);
        vSetHere.setOnClickListener(_v -> setLocationHere());
        vListTags = root.findViewById(R.id.listTags);
        vEditTags = root.findViewById(R.id.editTags);
        //vEditTags.setOnClickListener(_v -> onEditTagsClick());

        String title;
        Bundle args = getArguments();
        if (!args.getBoolean("new")) {
            long id = args.getLong("id");
            Database db = ((MainActivity) getActivity()).getDatabase();
            LocationEntity data = db.queryLocation(id);

            title = "Edit location";
            vDesc.setText(data.desc);
            vLat.setText(String.valueOf(data.lat));
            vLon.setText(String.valueOf(data.lon));
            vListTags.setText(String.valueOf(data.tagName));
        } else {
            title = "Create a new location";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setView(root);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            onOkClick();
        });
        builder.setNegativeButton(android.R.string.cancel, (_d, _w) -> {
        });

        return builder.create();
    }

    private void setLocationHere() {
        if (locationListener != null) {
            return;
        }
        locationListener = (status, gps_lat, gps_lon) -> {
            switch (status) {
                case WAITING:
                    vSetHere.setText("...");
                    break;
                case NO_PERMISSION:
                    Toast.makeText(getActivity(), "No permission!", Toast.LENGTH_SHORT).show();
                    vSetHere.setText("H");
                    locationProvider.stopRequest();
                    locationListener = null;
                    break;
                case GOT_DATA:
                    vLat.setText(String.valueOf(gps_lat));
                    vLon.setText(String.valueOf(gps_lon));
                    locationProvider.stopRequest();
                    locationListener = null;
                    break;
            }
        };
        locationProvider.startRequest(locationListener);
    }

    private void onOkClick() {
        Database db = ((MainActivity) getActivity()).getDatabase();

        String desc = vDesc.getText().toString();
        double lat = Double.parseDouble(vLat.getText().toString());
        double lon = Double.parseDouble(vLon.getText().toString());
        long tag = 0; // TODO

        Bundle args = getArguments();
        if (args.getBoolean("new")) {
            db.insertLocation(desc, lat, lon, tag);
        } else {
            db.updateLocation(args.getLong("id"), desc, lat, lon, tag);
        }

        Fragment fragment = getTargetFragment();
        if (fragment instanceof LocationManagementFragment) {
            ((LocationManagementFragment) fragment).notifyDataChanged();
        }
    }
}
