package com.accountant.accountant.managementview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.accountant.accountant.MainActivity;
import com.accountant.accountant.datalistview.EditDataDialog;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.TagList;

import java.util.ArrayList;
import java.util.List;

public class EditTagsDialog extends DialogFragment {
    public static final String ARG_MUST_SELECT = "must_select";
    public static final String ARG_HAS_CHECKED_TAG = "has_tag";
    public static final String ARG_CHECKED_TAG = "checked";

    private Long selectedTag;
    private List<Long> ids;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        boolean mustSelect = args.getBoolean(ARG_MUST_SELECT);

        if (args.getBoolean(ARG_HAS_CHECKED_TAG, false)) {
            selectedTag = args.getLong(ARG_CHECKED_TAG);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select tag");

        Database db = ((MainActivity) requireActivity()).getDatabase();
        TagList tagList = db.queryTagList();
        if (mustSelect) {
            createTagListMustSelect(builder, tagList);
        } else {
            createTagList(builder, tagList);
        }

        builder.setPositiveButton(android.R.string.ok, (_v, _i) -> {
            onOkClick();
        });
        builder.setNegativeButton(android.R.string.cancel, (_v, _i) -> {
        });
        return builder.create();
    }

    private void createTagList(AlertDialog.Builder builder, TagList tagList) {
        int count = tagList.getCount();

        List<String> tagNames = tagList.getNames();
        CharSequence[] items = new CharSequence[tagNames.size() + 1];
        items[0] = "<No tag>";
        for (int i = 1; i < count + 1; i++) {
            items[i] = tagNames.get(i - 1);
        }
        ids = new ArrayList<>(tagList.getIds());
        ids.add(0, -1L);

        boolean[] checked = new boolean[count + 1];
        int checkedItem = -1;

        if (selectedTag != null) {
            for (int i = 1; i < ids.size(); i++) {
                checked[i] = (long) selectedTag == ids.get(i);
                if (checked[i]) {
                    checkedItem = i;
                }
            }
        } else {
            checkedItem = 0;
        }

        builder.setSingleChoiceItems(items, checkedItem, (_d, i) -> {
            onTagClickedSingle(i);
        });
    }

    private void createTagListMustSelect(AlertDialog.Builder builder, TagList tagList) {
        int count = tagList.getCount();

        List<String> tagNames = tagList.getNames();
        CharSequence[] items = new CharSequence[tagNames.size()];
        for (int i = 0; i < tagNames.size(); i++) {
            items[i] = tagNames.get(i);
        }
        ids = tagList.getIds();

        boolean[] checked = new boolean[count];
        int checkedItem = -1;

        if (selectedTag != null) {
            for (int i = 1; i < ids.size(); i++) {
                checked[i] = (long) selectedTag == ids.get(i);
                if (checked[i]) {
                    checkedItem = i;
                }
            }
        }

        builder.setSingleChoiceItems(items, checkedItem, (_d, i) -> {
            onTagClickedSingle(i);
        });
    }

    private void onTagClickedSingle(int i) {
        boolean mustSelect = getArguments().getBoolean(ARG_MUST_SELECT);

        if (mustSelect) {
            selectedTag = ids.get(i);
        } else {
            if (i == 0) {
                selectedTag = null;
            } else {
                selectedTag = ids.get(i);
            }
        }
    }

    private void onOkClick() {
        Fragment target = getTargetFragment();
        if (target instanceof EditDataDialog) {
            ((EditDataDialog) target).updateTag(selectedTag);
        } else if (target instanceof EditLocationDialog) {
            ((EditLocationDialog) target).updateTag(selectedTag);
        }
    }
}
