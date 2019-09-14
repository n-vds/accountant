package com.accountant.accountant.managementview;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import com.accountant.accountant.MainActivity;
import com.accountant.accountant.R;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.TagEntry;

public class TagManagementFragment extends ListFragment {

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Database db = activity.getDatabase();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity,
                android.R.layout.simple_list_item_1,
                db.queryAllTagNames(),
                new String[]{TagEntry.NAME},
                new int[]{android.R.id.text1}, 0);

        setListAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tags, container, false);
        root.findViewById(R.id.add).setOnClickListener((_v) -> onAddClick());
        return root;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        showNameDialog(true, id, ((TextView) v).getText().toString());
    }

    private void onAddClick() {
        showNameDialog(false, 0, null);
    }

    private void addTag(String tagName) {
        Database db = ((MainActivity) getActivity()).getDatabase();
        db.insertTag(tagName);

        reload();
    }

    private void editTag(long id, String newTagName) {
        Database db = ((MainActivity) getActivity()).getDatabase();
        db.editTagName(id, newTagName);

        reload();
    }

    private void reload() {
        Database db = ((MainActivity) getActivity()).getDatabase();
        CursorAdapter adapter = ((CursorAdapter) getListAdapter());
        adapter.changeCursor(db.queryAllTagNames());
        adapter.notifyDataSetChanged();
    }

    private void showNameDialog(boolean edit, long id, String oldName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(edit ? "Edit tag" : "Add a new tag");

        EditText input = new EditText(getActivity());
        input.setHint("Name");
        if (edit) {
            input.setText(oldName);
            input.setSelection(0, oldName.length()); // stop is exclusive
        }
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, (_d, _i) -> {
            String tagName = input.getText().toString();

            if (tagName.isEmpty()) {
                Toast.makeText(getActivity(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (edit) {
                editTag(id, tagName);
            } else {
                addTag(tagName);
            }
        });

        builder.setNegativeButton(android.R.string.cancel, (_d, i_) -> {
        });

        builder.show();

        new Handler().post(() -> {
            input.requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        });
    }
}
