package com.accountant.accountant.view.datalist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.ListFragment;
import com.accountant.accountant.MainActivity;
import com.accountant.accountant.R;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.SpendingEntry;
import com.accountant.accountant.db.TagEntry;
import com.accountant.accountant.view.DeleteSelectedActionMode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataListFragment extends ListFragment {
    private static DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();

    private ActionMode selectDeleteActionMode = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Database db = activity.getDatabase();

        getListView().setOnItemLongClickListener((_adapterView, _view, pos, _id) -> {
            if (selectDeleteActionMode == null) {
                beginActionMode();
                getListView().setItemChecked(pos, true);
            }
            return true;
        });
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity,
                R.layout.data_list_row,
                db.queryDataForUserView(),
                new String[]{SpendingEntry.DATE, SpendingEntry.AMOUNT, SpendingEntry.AMOUNT, TagEntry.NAME},
                new int[]{R.id.date, R.id.amount, R.id.small_amount, R.id.listTags}, 0);

        adapter.setViewBinder((view, cursor, columnIndex) -> {
            TextView v = (TextView) view;

            switch (view.getId()) {
                case R.id.date:
                    long date = cursor.getLong(columnIndex);
                    v.setText(DATE_FORMAT.format(new Date(date)));
                    break;

                case R.id.amount:
                    v.setText(String.valueOf(cursor.getInt(columnIndex) / 100));
                    break;

                case R.id.small_amount:
                    v.setText(String.format("%02d", cursor.getInt(columnIndex) % 100));
                    break;

                case R.id.listTags:
                    if (cursor.isNull(columnIndex) || cursor.getString(columnIndex).isEmpty()) {
                        v.setText("");
                    } else {
                        v.setText(cursor.getString(columnIndex));
                    }
                    break;

                default:
                    return false;
            }
            return true;
        });

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        if (selectDeleteActionMode != null) {
            int count = getListView().getCheckedItemCount();
            if (count == 0) {
                selectDeleteActionMode.finish();
            }
            selectDeleteActionMode.setTitle(count + " selected");
            return;
        }
        EditDataDialog dialog = new EditDataDialog();
        Bundle args = new Bundle();
        args.putLong("id", id);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "editdatadialog");
    }

    private void beginActionMode() {
        DeleteSelectedActionMode callback = new DeleteSelectedActionMode(getListView(), this::deleteSelectedEntries);
        callback.setOnDestroyListener(() -> {
            selectDeleteActionMode = null;
        });
        selectDeleteActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(callback);
    }

    private void deleteSelectedEntries() {
        long[] ids = getListView().getCheckedItemIds();

        Database db = ((MainActivity) getActivity()).getDatabase();
        for (long id : ids) {
            db.deleteEntry(id);
        }
        notifyDataChanged();
    }

    void notifyDataChanged() {
        CursorAdapter adapter = (CursorAdapter) getListAdapter();
        Database db = ((MainActivity) getActivity()).getDatabase();
        adapter.changeCursor(db.queryDataForUserView());
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
    }
}
