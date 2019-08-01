package com.accountant.accountant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.Collection;

public class EditTagsDialog extends DialogFragment {
    private Collection<Long> selectedTags;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<String> tagsNames = getArguments().getStringArrayList("tagNames");
        long[] tagIds = getArguments().getLongArray("tagIds");
        boolean[] checked = getArguments().getBooleanArray("checked");

        selectedTags = new ArrayList<>();
        for (int i = 0; i < tagIds.length; i++) {
            if (checked[i]) {
                selectedTags.add(tagIds[i]);
            }
        }

        String[] tagNamesArr = tagsNames.toArray(new String[tagsNames.size()]);
        CharSequence[] tagNamesArrCs = tagNamesArr;

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMultiChoiceItems(tagNamesArrCs, checked, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedTags.add(tagIds[which]);
            } else {
                selectedTags.remove(tagIds[which]);
            }

        });
        builder.setPositiveButton(android.R.string.ok, (_v, _i) -> {
            Fragment target = getTargetFragment();
            if (target instanceof EditDataDialog) {
                ((EditDataDialog) target).updateTags(selectedTags);
            }

        });
        builder.setNegativeButton(android.R.string.cancel, (_v, _i) -> {
        });
        return builder.create();
    }
}
