package com.accountant.accountant;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
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
                new String[]{TagEntry.COLUMN_NAME},
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

    private void onAddClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add a new tag");

        EditText input = new EditText(getActivity());
        builder.setView(input);

        builder.setPositiveButton(android.R.string.ok, (_d, _i) -> {
            addTag(input.getText().toString());
        });

        builder.setNegativeButton(android.R.string.cancel, (_d, i_) -> {
        });

        builder.show();
        input.requestFocus();
    }

    private void addTag(String tagName) {
        if (tagName.isEmpty()) {
            Toast.makeText(getActivity(), "Can't add empty tag", Toast.LENGTH_SHORT).show();
            return;
        }

        Database db = ((MainActivity) getActivity()).getDatabase();
        db.insertTag(tagName);

        CursorAdapter adapter = ((CursorAdapter) getListAdapter());
        adapter.changeCursor(db.queryAllTagNames());
        adapter.notifyDataSetChanged();
    }
}
