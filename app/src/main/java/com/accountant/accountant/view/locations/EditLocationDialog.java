package com.accountant.accountant.view.locations;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.accountant.accountant.LocationProvider;
import com.accountant.accountant.MainActivity;
import com.accountant.accountant.R;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.LocationEntity;
import com.accountant.accountant.db.TagList;
import com.accountant.accountant.view.EditTagsDialog;

public class EditLocationDialog extends DialogFragment {
    private LocationProvider locationProvider;
    private LocationProvider.LocationProviderUpdate locationListener;

    private EditText vDesc;
    private EditText vLat;
    private EditText vLon;
    private View vSetHere;
    private TextView vListTags;
    private Button vEditTags;

    private Long selectedTag = null;
    private TagList tagList;

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
        vEditTags.setOnClickListener(_v -> onEditTagsClick());

        Database db = ((MainActivity) getActivity()).getDatabase();
        Bundle args = getArguments();

        String title;
        if (!args.getBoolean("new")) {
            long id = args.getLong("id");
            LocationEntity data = db.queryLocation(id);
            selectedTag = data.tag;

            title = "Edit location";
            vDesc.setText(data.desc);
            vLat.setText(String.valueOf(data.lat));
            vLon.setText(String.valueOf(data.lon));
            vListTags.setText(String.valueOf(data.tagName));
        } else {
            title = "Create a new location";
        }

        tagList = db.queryTagList();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setView(root)
                .setPositiveButton(android.R.string.ok, (_d, _w) -> {
                    // Do nothing
                    // this gets overridden in onResume
                })
                .setNegativeButton(android.R.string.cancel, (_d, _w) -> {
                });

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getDialog() != null) {
            AlertDialog dialog = ((AlertDialog) getDialog());
            // Set the listener here,
            // so the dialog does not get dismissed automatically
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((_v) -> {
                onOkClick(dialog);
            });
        }
    }

    private void setLocationHere() {
        if (locationListener != null) {
            return;
        }
        locationListener = (status, gps_lat, gps_lon) -> {
            switch (status) {
                case WAITING:
                    vSetHere.setEnabled(false);
                    break;
                case NO_PERMISSION:
                    Toast.makeText(getActivity(), "No permission!", Toast.LENGTH_SHORT).show();
                    vSetHere.setEnabled(true);
                    locationProvider.stopRequest();
                    locationListener = null;
                    break;
                case GOT_DATA:
                    vLat.setText(String.valueOf(gps_lat));
                    vLon.setText(String.valueOf(gps_lon));
                    locationProvider.stopRequest();
                    vSetHere.setEnabled(true);
                    locationListener = null;
                    break;
            }
        };
        locationProvider.startRequest(locationListener);
    }

    private void onOkClick(DialogInterface dialog) {
        Database db = ((MainActivity) getActivity()).getDatabase();

        String desc = vDesc.getText().toString();
        if (desc.isEmpty()) {
            Toast.makeText(getActivity(), "Input a valid name", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat, lon;
        try {
            lat = Double.parseDouble(vLat.getText().toString());
            lon = Double.parseDouble(vLon.getText().toString());
        } catch (NumberFormatException _e) {
            Toast.makeText(getActivity(), "Invalid location", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTag == null) {
            Toast.makeText(getActivity(), "Select a tag", Toast.LENGTH_SHORT).show();
            return;
        }
        long tag = selectedTag;

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

        dialog.dismiss();
    }

    private void onEditTagsClick() {
        EditTagsDialog dialog = new EditTagsDialog();

        Bundle args = new Bundle();
        if (selectedTag == null) {
            args.putBoolean(EditTagsDialog.ARG_HAS_CHECKED_TAG, false);
        } else {
            args.putBoolean(EditTagsDialog.ARG_HAS_CHECKED_TAG, true);
            args.putLong(EditTagsDialog.ARG_CHECKED_TAG, selectedTag);
        }
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "locationedittags");
    }

    public void updateTag(Long selectedTag) {
        if (selectedTag == null) {
            this.selectedTag = null;
            vListTags.setText("<Not tag>");
        } else {
            this.selectedTag = selectedTag;
            vListTags.setText(tagList.getName(selectedTag));
        }
    }
}
