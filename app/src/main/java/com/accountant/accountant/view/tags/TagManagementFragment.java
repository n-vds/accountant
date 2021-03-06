package com.accountant.accountant.view.tags;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.ListFragment;
import com.accountant.accountant.MainActivity;
import com.accountant.accountant.R;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.TagEntry;
import com.accountant.accountant.view.DeleteSelectedActionMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TagManagementFragment extends ListFragment {
    private FloatingActionButton fab;
    private ActionMode selectDeleteActionMode = null;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Database db = activity.getDatabase();

        getListView().setOnItemLongClickListener((_list, _view, position, _l) -> {
            beginActionMode();
            getListView().setItemChecked(position, true);
            return true;
        });

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity,
                android.R.layout.simple_list_item_activated_1,
                db.queryAllTagNames(),
                new String[]{TagEntry.NAME},
                new int[]{android.R.id.text1}, 0);

        setListAdapter(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tags, container, false);
        fab = root.findViewById(R.id.add);
        fab.setOnClickListener((_v) -> onAddClick());
        return root;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        if (selectDeleteActionMode == null) {
            showNameDialog(true, id);
        } else {
            int count = getListView().getCheckedItemCount();
            selectDeleteActionMode.setTitle(count + " selected");
            if (count == 0) {
                selectDeleteActionMode.finish();
            }
        }
    }

    private void onAddClick() {
        showNameDialog(false, 0L);
    }

    private void beginActionMode() {
        if (selectDeleteActionMode != null) {
            return;
        }
        DeleteSelectedActionMode callback = new DeleteSelectedActionMode(getListView(), this::deleteSelectedTags);
        callback.setOnDestroyListener(() -> {
            fab.show();
            selectDeleteActionMode = null;
        });
        selectDeleteActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(callback);
        fab.hide();
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

    private void deleteSelectedTags() {
        Database db = ((MainActivity) getActivity()).getDatabase();
        for (long id : getListView().getCheckedItemIds()) {
            db.deleteTag(id);
        }
        reload();
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
