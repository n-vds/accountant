package com.accountant.accountant;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.accountant.accountant.db.Database;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int[] R_BUTTONS = new int[]{
            R.id.button0, R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5,
            R.id.button6, R.id.button7, R.id.button8, R.id.button9
    };

    private TextView vInput;
    private int inputAmount;
    private Button[] inputButtons;
    private Button buttonDot, buttonGo, buttonDel;

    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputButtons = new Button[R_BUTTONS.length];
        for (int i = 0; i < R_BUTTONS.length; i++) {
            inputButtons[i] = findViewById(R_BUTTONS[i]);
            inputButtons[i].setOnClickListener(this::onClick);
        }
        buttonDot = findViewById(R.id.buttonDot);
        buttonDot.setOnClickListener(this::onClick);
        buttonGo = findViewById(R.id.buttonGo);
        buttonGo.setOnClickListener(_v -> onGoClick());
        buttonDel = findViewById(R.id.buttonDel);
        buttonDel.setOnClickListener(_v -> onDelClick());
        vInput = findViewById(R.id.viewAmount);
        inputAmount = 0;

        db = new Database(this);
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
        db.insert(inputAmount * 100);
        inputAmount = 0;
        handleAmountChange();
        Toast.makeText(this, "Inserted amount", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
