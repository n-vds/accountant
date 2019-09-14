package com.accountant.accountant.view.datalist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import com.accountant.accountant.MainActivity;
import com.accountant.accountant.R;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.SpendingEntity;
import com.accountant.accountant.db.TagList;
import com.accountant.accountant.view.EditTagsDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

public class EditDataDialog extends DialogFragment {
    private EditText vDate;
    private EditText vAmount;
    private TextView vTagName;
    private Button vChangeTag;

    private TagList tagList;
    private Long selectedTag;

    private SpendingEntity originalData;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long id = getArguments().getLong("id");

        Database db = ((MainActivity) getActivity()).getDatabase();
        tagList = db.queryTagList();
        originalData = db.querySingle(id);

        selectedTag = originalData.tagId;

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_editdata, null);

        vDate = view.findViewById(R.id.date);
        vAmount = view.findViewById(R.id.amount);
        vTagName = view.findViewById(R.id.tagName);
        vChangeTag = view.findViewById(R.id.changeTag);
        vDate.setText(DateFormat.getDateTimeInstance().format(new Date(originalData.timestamp)));
        vAmount.setText(String.valueOf(originalData.amount / 100));

        vTagName.setText(selectedTag == null ? "<No tag>" : tagList.getName(selectedTag));
        vChangeTag.setOnClickListener((_v) -> onChangeTagClicked());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit entry");
        builder.setView(view);
        builder.setPositiveButton(android.R.string.ok, (_d, _i) -> onOkClick());
        builder.setNegativeButton(android.R.string.cancel, (_d, _i) -> {
        });
        return builder.create();
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

        db.updateEntry(id, date, amount, selectedTag);

        // Notify caller
        Fragment fragment = getTargetFragment();
        if (fragment instanceof DataListFragment) {
            ((DataListFragment) fragment).notifyDataChanged();
        }
    }

    private void onChangeTagClicked() {
        EditTagsDialog dialog = new EditTagsDialog();
        dialog.setTargetFragment(this, 0);

        Bundle args = new Bundle();
        if (selectedTag != null) {
            args.putBoolean(EditTagsDialog.ARG_HAS_CHECKED_TAG, true);
            args.putLong(EditTagsDialog.ARG_CHECKED_TAG, selectedTag);
        } else {
            args.putBoolean(EditTagsDialog.ARG_HAS_CHECKED_TAG, false);
        }
        dialog.setArguments(args);

        dialog.show(getFragmentManager(), "edittags");
    }

    public void updateTag(Long tag) {
        selectedTag = tag;
        vTagName.setText(selectedTag == null ? "<No tag>" : tagList.getName(tag));
    }
}
