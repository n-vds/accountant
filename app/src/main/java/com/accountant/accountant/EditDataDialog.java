package com.accountant.accountant;

import android.app.AlertDialog;
import android.app.Dialog;
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
import com.accountant.accountant.db.TagList;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class EditDataDialog extends DialogFragment {
    private EditText vDate;
    private EditText vAmount;
    private Button vTags;

    private TagList tagList;
    private Set<Long> selectedTagIds;

    private SpendingEntity originalData;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long id = getArguments().getLong("id");

        Database db = ((MainActivity) getActivity()).getDatabase();
        tagList = db.queryTagList();
        originalData = db.querySingle(id);

        selectedTagIds = new HashSet<>();
        for (long tag : originalData.tagIds) {
            selectedTagIds.add(tag);
        }

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
        });
        return builder.create();
    }

    private String buildTagString() {
        if (selectedTagIds.isEmpty()) {
            return "<No tags>";
        }

        StringBuilder b = new StringBuilder();
        boolean first = true;
        for (long tagId : selectedTagIds) {
            if (!first) {
                b.append(", ");
            }

            String tagName = tagList.getName(tagId);
            if (tagName == null) {
                b.append("<unknown #").append(tagId).append(">");
            } else {
                b.append(tagName);
            }
            first = false;
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
        EditTagsDialog dialog = new EditTagsDialog();
        dialog.setTargetFragment(this, 0);

        Bundle args = new Bundle();
        args.putBoolean(EditTagsDialog.ARG_ONLY_SINGLE_TAG, false);
        args.putLongArray(EditTagsDialog.ARG_CHECKED_TAGS, toLongArray(selectedTagIds));
        dialog.setArguments(args);

        dialog.show(getFragmentManager(), "edittags");
    }

    public void updateTags(Collection<Long> selectedTags) {
        this.selectedTagIds.clear();
        this.selectedTagIds.addAll(selectedTags);
        vTags.setText(buildTagString());
    }

    private long[] toLongArray(Collection<Long> collection) {
        long[] arr = new long[collection.size()];
        int i = 0;
        for (long l : collection) {
            arr[i] = l;
            i++;
        }
        return arr;
    }
}
