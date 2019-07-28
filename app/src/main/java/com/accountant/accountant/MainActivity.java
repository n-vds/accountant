package com.accountant.accountant;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.accountant.accountant.db.Database;

public class MainActivity extends AppCompatActivity {

    private static final int[] R_BUTTONS = new int[]{
            R.id.button0, R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5,
            R.id.button6, R.id.button7, R.id.button8, R.id.button9
    };

    private String inputString;

    private EditText vInput;
    private Button[] inputButtons;
    private Button buttonDot, buttonGo;

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
        buttonGo.setOnClickListener(this::onClick);
        vInput = findViewById(R.id.editText);
        vInput.setShowSoftInputOnFocus(false);

        inputString = "";

        db = new Database(this);
    }


    private void onClick(View which) {
        for (int i = 0; i < R_BUTTONS.length; i++) {
            if (inputButtons[i] == which) {
                inputString += String.valueOf(i);
            }
        }

        if (which == buttonDot) {
            inputString = inputString.replaceFirst("\\.", "");
            inputString += ".";
        } else if (which == buttonGo) {
            //TODO
        }

        vInput.setText(inputString);
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
