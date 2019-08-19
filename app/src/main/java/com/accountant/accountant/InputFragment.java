package com.accountant.accountant;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.DistanceLocationEntity;
import com.accountant.accountant.db.LocationEntity;

import java.util.Locale;

public class InputFragment extends Fragment {
    private static final int[] R_BUTTONS = new int[]{
            R.id.button0, R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5,
            R.id.button6, R.id.button7, R.id.button8, R.id.button9
    };

    private TextView vInput;
    private TextView locationMessage;
    private int inputAmount;
    private Button[] inputButtons;
    private Button buttonDot, buttonGo, buttonDel;

    private LocationEntity knownLocation;

    private LocationProvider locationProvider;
    private LocationProvider.LocationProviderUpdate locationListener;

    public InputFragment() {
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
        buttonDot.setOnClickListener(this::onClick);
        buttonGo = root.findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(_v -> onGoClick());
        buttonDel = root.findViewById(R.id.buttonDel);
        buttonDel.setOnClickListener(_v -> onDelClick());
        buttonDel.setOnLongClickListener(_v -> {
            onDelLongClick();
            return true;
        });
        vInput = root.findViewById(R.id.viewAmount);
        locationMessage = root.findViewById(R.id.locationMessage);
        locationMessage.setOnClickListener(_v -> onLocationMessageClicked());

        inputAmount = 0;

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
                onClickNumber(num);
                break;
            }
        }
    }

    private void onGoClick() {
        MainActivity activity = (MainActivity) getActivity();

        if (inputAmount == 0) {
            Toast.makeText(getActivity(), "You didn't specify an amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        Database db = activity.getDatabase();
        if (knownLocation == null)  {
            db.insert(inputAmount * 100);
        } else {
            db.insert(inputAmount * 100, knownLocation.tag);
        }

        inputAmount = 0;
        handleAmountChange();
        activity.switchToData();
    }

    private void onDelClick() {
        inputAmount = inputAmount / 10;
        handleAmountChange();
    }

    private void onDelLongClick() {
        inputAmount = 0;
        handleAmountChange();
    }

    private void onClickNumber(int num) {
        inputAmount = inputAmount * 10 + num;
        handleAmountChange();
    }

    private void handleAmountChange() {
        if (inputAmount < 0) {
            inputAmount = 0;
        } else if (inputAmount > 999_999) {
            inputAmount = 999_999;
        }
        String formatted = String.format(Locale.getDefault(), "%,d €", inputAmount);
        vInput.setText(formatted);
    }

}
