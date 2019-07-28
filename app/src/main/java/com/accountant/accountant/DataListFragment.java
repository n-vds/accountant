package com.accountant.accountant;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
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
                db.queryData(),
                new String[]{SpendingEntry.COLUMN_DATE, SpendingEntry.COLUMN_AMOUNT},
                new int[]{R.id.date, R.id.amount}, 0);

        adapter.setViewBinder((view, cursor, columnIndex) -> {
            TextView v = (TextView) view;

            if (columnIndex == cursor.getColumnIndex(SpendingEntry.COLUMN_DATE)) {
                long date = cursor.getLong(columnIndex);
                v.setText(DATE_FORMAT.format(new Date(date)));
            } else if (columnIndex == cursor.getColumnIndex(SpendingEntry.COLUMN_AMOUNT)) {
                v.setText((cursor.getInt(columnIndex) / 100) + " €");
            } else {
                return false;
            }

            return true;
        });

        setListAdapter(adapter);
    }
}
