package com.accountant.accountant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.SpendingEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataListFragment extends ListFragment {

    private static DateFormat DATE_FORMAT = SimpleDateFormat.getDateTimeInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Database db = activity.getDatabase();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity,
                R.layout.data_list_row,
                db.queryDataForUserView(),
                new String[]{SpendingEntry.COLUMN_DATE, SpendingEntry.COLUMN_AMOUNT, Database.COLUMN_TAG_LIST},
                new int[]{R.id.date, R.id.amount, R.id.listTags}, 0);

        adapter.setViewBinder((view, cursor, columnIndex) -> {
            TextView v = (TextView) view;

            if (columnIndex == cursor.getColumnIndex(SpendingEntry.COLUMN_DATE)) {
                long date = cursor.getLong(columnIndex);
                v.setText(DATE_FORMAT.format(new Date(date)));
            } else if (columnIndex == cursor.getColumnIndex(SpendingEntry.COLUMN_AMOUNT)) {
                v.setText((cursor.getInt(columnIndex) / 100) + " €");
            } else if (columnIndex == cursor.getColumnIndex(Database.COLUMN_TAG_LIST)) {
                String tags = cursor.getString(columnIndex);
                if (tags == null || tags.isEmpty()) {
                    v.setText("Click to add tags");
                } else {
                    v.setText(tags);
                }
            } else {
                return false;
            }

            return true;
        });

        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView list, View v, int position, long id) {
        EditDataDialog dialog = new EditDataDialog();
        Bundle args = new Bundle();
        args.putLong("id", id);
        dialog.setArguments(args);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "editdatadialog");
    }

    void notifyDataChanged() {
        CursorAdapter adapter = (CursorAdapter) getListAdapter();
        Database db = ((MainActivity) getActivity()).getDatabase();
        adapter.changeCursor(db.queryDataForUserView());
        ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
    }
}
