package com.accountant.accountant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.SpendingEntity;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class EditDataDialog extends DialogFragment {
    private EditText vDate;
    private EditText vAmount;
    private EditText vTags;

    private SpendingEntity originalData;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long id = getArguments().getLong("id");

        Database db = ((MainActivity) getActivity()).getDatabase();
        originalData = db.querySingle(id);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editdata, null);

        vDate = view.findViewById(R.id.date);
        vAmount = view.findViewById(R.id.amount);
        vTags = view.findViewById(R.id.listTags);
        vDate.setText(DateFormat.getDateTimeInstance().format(new Date(originalData.timestamp)));
        vAmount.setText(String.valueOf(originalData.amount / 100));
        vTags.setText(joinToString(originalData.tagNames));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, (_d, _i) -> onOkClick());
        builder.setNegativeButton(android.R.string.cancel, (_d, _i) -> {
            // do nothing on cancel
        });
        return builder.create();
    }

    private static String joinToString(String[] strs) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            if (i != 0) {
                b.append(',').append(' ');
            }
            b.append(strs[i]);
        }
        return b.toString();
    }

    private void onOkClick() {
        Database db = ((MainActivity) getActivity()).getDatabase();

        long id = getArguments().getLong("id");
        long date = originalData.timestamp;
        try {
            date = DateFormat.getDateTimeInstance().parse(vDate.getText().toString()).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int amount = Integer.parseInt(vAmount.getText().toString()) * 100;
        db.updateEntry(id, date, amount, originalData.tagIds);

        // Notify caller
        Fragment fragment = getTargetFragment();
        if (fragment instanceof DataListFragment) {
            ((DataListFragment) fragment).notifyDataChanged();
        }
    }
}
