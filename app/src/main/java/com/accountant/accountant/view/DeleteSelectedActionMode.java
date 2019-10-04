package com.accountant.accountant.view;

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

    public DeleteSelectedActionMode(ListView view) {
        this.listView = view;
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
                // TODO
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        listView.clearChoices();
        new Handler().post(() -> {
            listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
            listView.requestLayout();
            ((BaseAdapter) listView.getAdapter()).notifyDataSetChanged();
        });
        if (this.onDestroy != null) {
            this.onDestroy.run();
        }
    }
}
