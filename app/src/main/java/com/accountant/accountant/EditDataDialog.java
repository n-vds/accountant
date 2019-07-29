package com.accountant.accountant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.SpendingEntity;
import com.accountant.accountant.db.SpendingEntry;

public class EditDataDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long id = getArguments().getLong("id");

        Database db = ((MainActivity) getActivity()).getDatabase();
        SpendingEntity entity = db.querySingle(id);

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editdata, null);

        ((EditText) view.findViewById(R.id.date)).setText("01.01.2019 01:01:00");
        ((EditText) view.findViewById(R.id.amount)).setText("" + entity.amount);
        ((EditText) view.findViewById(R.id.listTags)).setText(joinToString(entity.tagNames));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, (_d, _i) -> onOkClick());
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
        //TODO: db.updateEntry()
    }
}
