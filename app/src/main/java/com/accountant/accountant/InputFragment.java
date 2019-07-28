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

import java.util.Locale;

public class InputFragment extends Fragment {
    private static final int[] R_BUTTONS = new int[]{
            R.id.button0, R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5,
            R.id.button6, R.id.button7, R.id.button8, R.id.button9
    };

    private TextView vInput;
    private int inputAmount;
    private Button[] inputButtons;
    private Button buttonDot, buttonGo, buttonDel;

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
        vInput = root.findViewById(R.id.viewAmount);

        inputAmount = 0;

        return root;
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

        activity.getDatabase().insert(inputAmount * 100);
        inputAmount = 0;
        handleAmountChange();
        activity.switchToData();
    }

    private void onDelClick() {
        inputAmount = inputAmount / 10;
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
