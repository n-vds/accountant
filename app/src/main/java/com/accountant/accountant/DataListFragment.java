package com.accountant.accountant;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.SpendingEntry;

public class DataListFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        Database db = activity.getDatabase();

        Log.d(DataListFragment.class.getSimpleName(), "Data count is " + db.getData().getCount());
        Cursor cursor = db.getData();

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(activity,
                R.layout.data_list_row,
                cursor, new String[]{SpendingEntry.COLUMN_DATE, SpendingEntry.COLUMN_AMOUNT},
                new int[]{R.id.date, R.id.amount}, 0);

        setListAdapter(adapter);
    }
}
