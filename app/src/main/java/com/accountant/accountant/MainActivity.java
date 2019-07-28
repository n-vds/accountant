package com.accountant.accountant;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.accountant.accountant.db.Database;

public class MainActivity extends AppCompatActivity {

    private static final int[] R_BUTTONS = new int[]{
            R.id.button0, R.id.button1, R.id.button2,
            R.id.button3, R.id.button4, R.id.button5,
            R.id.button6, R.id.button7, R.id.button8, R.id.button9
    };

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

        db = new Database(this);
    }


    private void onClick(View which) {
        Editable text = vInput.getText();
        CharSequence numeric = text.subSequence(0, text.length() - 2);

        for (int i = 0; i < R_BUTTONS.length; i++) {
            if (inputButtons[i] == which) {
                if (numeric.equals("0") && i == 0) {
                    break;
                }
                vInput.setText(numeric + " €");
                break;
            }
        }

        if (which == buttonGo) {
            db.insert(Integer.parseInt(numeric.toString()));
            vInput.setText("0 €");
            Toast.makeText(this, "Inserted amount", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
