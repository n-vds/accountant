package com.accountant.accountant.view.insert;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.accountant.accountant.LocationProvider;
import com.accountant.accountant.MainActivity;
import com.accountant.accountant.R;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.DistanceLocationEntity;
import com.accountant.accountant.db.LocationEntity;
import com.accountant.accountant.view.EditTagsDialog;

public class InsertFragment extends Fragment {
    private static final int[] R_BUTTONS = new int[]{
            R.id.button0, R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5,
            R.id.button6, R.id.button7, R.id.button8, R.id.button9
    };

    private TextView vInput;
    private TextView locationMessage;
    private Button[] inputButtons;
    private Button buttonDot, buttonGo, buttonDel;

    private LocationEntity knownLocation;

    private LocationProvider locationProvider;
    private LocationProvider.LocationProviderUpdate locationListener;

    private String inputString;

    public InsertFragment() {
        locationListener = (status, lat, lon) -> onLocationUpdate(status, lat, lon);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.locationProvider = ((MainActivity) getActivity()).getLocationProvider();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_input, container, false);

        inputButtons = new Button[R_BUTTONS.length];
        for (int i = 0; i < R_BUTTONS.length; i++) {
            inputButtons[i] = root.findViewById(R_BUTTONS[i]);
            inputButtons[i].setOnClickListener(this::onClick);
        }
        buttonDot = root.findViewById(R.id.buttonDot);
        buttonDot.setOnClickListener(_v -> onDotClick());
        buttonGo = root.findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(_v -> onGoClick());
        buttonGo.setOnLongClickListener(_v -> {
            onGoLongClick();
            return true;
        });
        buttonDel = root.findViewById(R.id.buttonDel);
        buttonDel.setOnClickListener(_v -> onDelClick());
        buttonDel.setOnLongClickListener(_v -> {
            onDelLongClick();
            return true;
        });
        vInput = root.findViewById(R.id.viewAmount);
        locationMessage = root.findViewById(R.id.locationMessage);
        locationMessage.setOnClickListener(_v -> onLocationMessageClicked());

        inputString = "0";
        handleAmountChange();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        locationProvider.startRequest(locationListener);
    }

    @Override
    public void onPause() {
        locationProvider.stopRequest();
        super.onPause();
    }

    private void onLocationMessageClicked() {
        ((MainActivity) getActivity()).requestLocationPermission();
    }

    private void onLocationUpdate(LocationProvider.Status status, double lat, double lon) {
        switch (status) {
            case INACTIVE:
                locationMessage.setText("...");
                break;
            case NO_PERMISSION:
                locationMessage.setText("Permission for location denied. Click to retry");
                break;
            case WAITING:
                locationMessage.setText("Waiting for location update...");
                break;
            case GOT_DATA:
                locationProvider.stopRequest();
                locationMessage.setText("" + lat + ", " + lon);
                resolveLocation(lat, lon);
                break;
        }
    }

    private void resolveLocation(double lat, double lon) {
        Database db = ((MainActivity) getActivity()).getDatabase();
        DistanceLocationEntity location = db.resolveLocation(lat, lon);
        if (location == null || location.distance > 150.0) {
            locationMessage.setText("Unknown location (" + lat + ", " + lon + ")");
        } else {
            locationMessage.setText(location.location.desc + " (" + Math.round(location.distance) + "m away)");
            knownLocation = location.location;
        }
    }

    private void onClick(View which) {
        for (int num = 0; num < R_BUTTONS.length; num++) {
            if (which.getId() == R_BUTTONS[num]) {
                onNumClick(num);
                break;
            }
        }
    }

    private void onGoClick() {
        // Insert without overriding tag => using location
        execInsert(false, null);
    }

    private void execInsert(boolean overrideTag, Long tag) {
        MainActivity activity = (MainActivity) getActivity();

        int dotpos = inputString.indexOf(".");
        int inputAmount;
        if (dotpos == -1) {
            inputAmount = Integer.parseInt(inputString) * 100;
        } else {
            inputAmount = Integer.parseInt(inputString.substring(0, dotpos)) * 100;
            if (dotpos < inputString.length() - 1) {
                int value = Integer.parseInt(inputString.substring(dotpos + 1));
                if (dotpos == inputString.length() - 2) {
                    value *= 10; // so xx.8 => 80
                }
                inputAmount += value;
            }
        }
        if (inputAmount == 0) {
            Toast.makeText(getActivity(), "You didn't specify an amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        Database db = activity.getDatabase();
        if (overrideTag) {
            db.insert(inputAmount, tag);
        } else if (knownLocation == null) {
            db.insert(inputAmount, null);
        } else {
            db.insert(inputAmount, knownLocation.tag);
        }

        inputString = "0";
        handleAmountChange();

        Navigation.findNavController(requireActivity(), R.id.content)
                .navigate(R.id.bottomNavFragmentList);
    }

    private void onGoLongClick() {
        Bundle args = new Bundle();
        args.putBoolean(EditTagsDialog.ARG_MUST_SELECT, false);
        args.putBoolean(EditTagsDialog.ARG_HAS_CHECKED_TAG, false);

        EditTagsDialog dialog = new EditTagsDialog();
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), null);
    }

    public void updateTag(Long tag) {
        execInsert(true, tag);
    }

    private void onDelClick() {
        if (inputString.length() == 1) {
            inputString = "0";
        } else {
            inputString = inputString.substring(0, inputString.length() - 1);
        }
        handleAmountChange();
    }

    private void onDelLongClick() {
        inputString = "0";
        handleAmountChange();
    }

    private void onNumClick(int num) {
        if (inputString.equals("0")) {
            inputString = String.valueOf(num);
        } else {
            int dotpos = inputString.indexOf(".");
            if (dotpos != -1 && dotpos < inputString.length() - 2) {
                return;
            }
            inputString = inputString + num;
        }
        handleAmountChange();
    }

    private void onDotClick() {
        if (inputString.indexOf('.') > -1) {
            return;
        }
        inputString = inputString + ".";
        handleAmountChange();
    }

    private void handleAmountChange() {
        if ((inputString + ".").indexOf('.') > 4) {
            inputString = "99999";
        }
        String formatted = inputString + " €";
        vInput.setText(formatted);
    }

}
