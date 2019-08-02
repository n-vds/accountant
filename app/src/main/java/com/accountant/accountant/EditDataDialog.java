package com.accountant.accountant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.SpendingEntity;
import com.accountant.accountant.db.TagEntry;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class EditDataDialog extends DialogFragment {
    private EditText vDate;
    private EditText vAmount;
    private Button vTags;

    private ArrayList<String> tagNames;
    private long[] tagIds;
    private List<Long> selectedTagIds;

    private SpendingEntity originalData;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long id = getArguments().getLong("id");

        Database db = ((MainActivity) getActivity()).getDatabase();
        originalData = db.querySingle(id);

        selectedTagIds = new ArrayList<>();
        for (long tag : originalData.tagIds) {
            selectedTagIds.add(tag);
        }

        getTags();

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editdata, null);

        vDate = view.findViewById(R.id.date);
        vAmount = view.findViewById(R.id.amount);
        vTags = view.findViewById(R.id.listTags);
        vDate.setText(DateFormat.getDateTimeInstance().format(new Date(originalData.timestamp)));
        vAmount.setText(String.valueOf(originalData.amount / 100));

        vTags.setText(buildTagString());
        vTags.setOnClickListener((_v) -> onTagsClick());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, (_d, _i) -> onOkClick());
        builder.setNegativeButton(android.R.string.cancel, (_d, _i) -> {
            // do nothing on cancel
        });
        return builder.create();
    }

    private void getTags() {
        Database db = ((MainActivity) getActivity()).getDatabase();

        Cursor cursor = db.queryAllTagNames();
        tagNames = new ArrayList<>(cursor.getCount());
        tagIds = new long[cursor.getCount()];

        cursor.moveToFirst();
        for (int i = 0; i < tagIds.length; i++) {
            tagNames.add(cursor.getString(cursor.getColumnIndex(TagEntry.COLUMN_NAME)));
            tagIds[i] = cursor.getLong(cursor.getColumnIndex(TagEntry.COLUMN_ID));
            cursor.moveToNext();
        }
    }

    private String buildTagString() {
        if (selectedTagIds.isEmpty()) {
            return "<No tags>";
        }

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < selectedTagIds.size(); i++) {
            if (i != 0) {
                b.append(", ");
            }

            boolean found = false;

            for (int j = 0; j < tagNames.size(); j++) {
                if (selectedTagIds.get(i) == tagIds[j]) {
                    b.append(tagNames.get(j));
                    found = true;
                    break;
                }
            }

            if (!found) {
                b.append("<unknown #").append(selectedTagIds.get(i)).append('>');
            }
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

        db.updateEntry(id, date, amount, selectedTagIds);

        // Notify caller
        Fragment fragment = getTargetFragment();
        if (fragment instanceof DataListFragment) {
            ((DataListFragment) fragment).notifyDataChanged();
        }
    }

    private void onTagsClick() {
        boolean[] checked = new boolean[tagIds.length];

        for (int i = 0; i < tagIds.length; i++) {
            checked[i] = selectedTagIds.contains(tagIds[i]);
        }

        EditTagsDialog dialog = new EditTagsDialog();
        dialog.setTargetFragment(this, 0);
        Bundle args = new Bundle();
        args.putStringArrayList("tagNames", tagNames);
        args.putLongArray("tagIds", tagIds);
        args.putBooleanArray("checked", checked);
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "edittags");
    }

    public void updateTags(Collection<Long> selectedTags) {
        this.selectedTagIds.clear();
        this.selectedTagIds.addAll(selectedTags);
        vTags.setText(buildTagString());
    }
}
