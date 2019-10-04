package com.accountant.accountant.view;

import android.app.AlertDialog;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import androidx.appcompat.view.ActionMode;
import com.accountant.accountant.R;

public class DeleteSelectedActionMode implements ActionMode.Callback {
    private ListView listView;
    private Runnable onDestroy = null;
    private Runnable onDelete = null;

    public DeleteSelectedActionMode(ListView view, Runnable onDelete) {
        this.listView = view;
        this.onDelete = onDelete;
    }

    public void setOnDestroyListener(Runnable listener) {
        this.onDestroy = listener;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mode.getMenuInflater().inflate(R.menu.actionbar_select, menu);
        mode.setTitle("1 selected");
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                askDelete(mode);
                return true;
            default:
                return false;
        }
    }

    private void askDelete(ActionMode mode) {
        int count = listView.getCheckedItemCount();
        if (count == 0) {
            return;
        }
        new AlertDialog.Builder(listView.getContext())
                .setTitle("Delete items")
                .setMessage("Do you want to delete " + count + " items?")
                .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> {})
                .setPositiveButton("Delete", (dialogInterface, i) -> {
                    onDelete.run();
                    mode.finish();
                })
                .show();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        listView.clearChoices();
        listView.requestLayout();
        ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        new Handler().post(() -> {
            listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
        });
        if (this.onDestroy != null) {
            this.onDestroy.run();
        }
    }
}
