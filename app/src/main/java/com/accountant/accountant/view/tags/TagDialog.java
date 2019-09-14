package com.accountant.accountant.view.tags;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.accountant.accountant.MainActivity;
import com.accountant.accountant.R;

public class TagDialog extends DialogFragment {
    public static final String ARG_EDIT = "tagdialog.edit";
    public static final String ARG_ID = "tagdialog.edit.id";

    private EditText vName;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        boolean edit = args.getBoolean(ARG_EDIT);
        long tagId = getArguments().getLong(ARG_ID, -1L);

        View root = requireActivity().getLayoutInflater().inflate(R.layout.dialog_edittag, null);
        vName = root.findViewById(R.id.tagName);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(edit ? "Edit tag" : "Add a new tag");

        if (edit) {
            String oldName = ((MainActivity) getActivity()).getDatabase().queryTagList().getName(tagId);
            vName.setText(oldName);
            vName.setSelection(0, oldName.length()); // stop is exclusive
        }
        builder.setView(root);

        builder.setPositiveButton(android.R.string.ok, (_d, _i) -> {
            String tagName = vName.getText().toString();

            if (tagName.isEmpty()) {
                Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            ((TagManagementFragment) getTargetFragment()).onTagDialogResult(tagName, edit, tagId);
        });

        builder.setNegativeButton(android.R.string.cancel, (_d, i_) -> {
        });

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        new Handler().post(() -> {
            vName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(vName, InputMethodManager.SHOW_IMPLICIT);
        });
    }
}
