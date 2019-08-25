package com.accountant.accountant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.TagList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditTagsDialog extends DialogFragment {
    public static final String ARG_ONLY_SINGLE_TAG = "onlySingle";
    public static final String ARG_CHECKED_TAGS = "checked";

    private Set<Long> selectedTags;
    private List<Long> ids;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        boolean selectSingleTag = args.getBoolean(ARG_ONLY_SINGLE_TAG, false);

        long[] checkedTagIds = args.getLongArray(ARG_CHECKED_TAGS);

        selectedTags = new HashSet<>();
        for (long tagId : checkedTagIds) {
            selectedTags.add(tagId);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Set tags");

        createTagList(builder, selectSingleTag);

        builder.setPositiveButton(android.R.string.ok, (_v, _i) -> {
            onOkClick();
        });
        builder.setNegativeButton(android.R.string.cancel, (_v, _i) -> {
        });
        return builder.create();
    }

    private void createTagList(AlertDialog.Builder builder, boolean singleOnly) {
        Database db = ((MainActivity) getActivity()).getDatabase();

        TagList tagList = db.queryTagList();
        int count = tagList.getCount();

        CharSequence[] items = tagList.getNames().toArray(new CharSequence[count]);
        ids = tagList.getIds();

        boolean[] checked = new boolean[count];
        int checkedItem = -1;

        for (int i = 0; i < ids.size(); i++) {
            checked[i] = selectedTags.contains(ids.get(i));
            if (checked[i]) {
                checkedItem = i;
            }
        }

        if (singleOnly) {
            builder.setSingleChoiceItems(items, checkedItem, (_d, i) -> {
                onTagClickedSingle(i);
            });
        } else {
            builder.setMultiChoiceItems(items, checked, (_d, which, isChecked) -> {
                onTagClickedMulti(which, isChecked);
            });
        }
    }

    private void onTagClickedSingle(int i) {
        long id = ids.get(i);
        selectedTags.clear();
        selectedTags.add(id);
    }

    private void onTagClickedMulti(int i, boolean isChecked) {
        long id = ids.get(i);

        if (isChecked) {
            selectedTags.add(id);
        } else {
            selectedTags.remove(id);
        }
    }

    private void onOkClick() {
        Fragment target = getTargetFragment();
        if (target instanceof EditDataDialog) {
            ((EditDataDialog) target).updateTags(selectedTags);
        } else if (target instanceof EditLocationDialog) {
            ((EditLocationDialog) target).updateTags(selectedTags);
        }
    }
}
