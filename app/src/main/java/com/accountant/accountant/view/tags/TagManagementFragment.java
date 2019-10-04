package com.accountant.accountant.view.tags;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                R.layout.edittag_tag_row,
                db.queryAllTagNames(),
                new String[]{TagEntry.NAME},
                new int[]{R.id.root}, 0);

        adapter.setViewBinder(((view, cursor, index) -> {
            TextView vTagName = view.findViewById(R.id.tagName);
            vTagName.setText(cursor.getString(cursor.getColumnIndex(TagEntry.NAME)));

            ImageView vDelete = view.findViewById(R.id.delete);
            vDelete.setTag(cursor.getLong(cursor.getColumnIndex(TagEntry.ID)));
            vDelete.setOnClickListener(this::onDeleteClicked);

            return true;
        }));

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
        showNameDialog(true, id);
    }

    private void onAddClick() {
        showNameDialog(false, 0L);
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

    private void onDeleteClicked(View view) {
        long id = (long) view.getTag();
        Database db = ((MainActivity) requireActivity()).getDatabase();
        String tagName = db.queryTagList().getName(id);

        new AlertDialog.Builder(requireContext())
                .setMessage("Do you want to delete '" + tagName + "'?")
                .setNegativeButton(android.R.string.cancel, (_d, _v) -> {})
                .setPositiveButton("Delete", (_d, _v) -> db.deleteTag(id))
                .show();
    }

    private void reload() {
        Database db = ((MainActivity) getActivity()).getDatabase();
        CursorAdapter adapter = ((CursorAdapter) getListAdapter());
        adapter.changeCursor(db.queryAllTagNames());
        adapter.notifyDataSetChanged();
    }

    private void showNameDialog(boolean edit, long id) {
        TagDialog dialog = new TagDialog();
        dialog.setTargetFragment(this, 0);
        Bundle args = new Bundle();
        if (edit) {
            args.putBoolean(TagDialog.ARG_EDIT, true);
            args.putLong(TagDialog.ARG_ID, id);
        } else {
            args.putBoolean(TagDialog.ARG_EDIT, false);
        }
        dialog.setArguments(args);
        dialog.show(getFragmentManager(), "edittag");
    }

    public void onTagDialogResult(String name, boolean edit, long id) {
        if (!edit) {
            addTag(name);
        } else {
            editTag(id, name);
        }
    }
}
