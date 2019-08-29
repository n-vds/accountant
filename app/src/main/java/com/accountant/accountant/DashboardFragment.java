package com.accountant.accountant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.StatisticsEntity;

public class DashboardFragment extends Fragment {
    private TextView tvThisMonth;
    private TextView tvLastThirtyDays;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvThisMonth = view.findViewById(R.id.dataThisMonth);
        tvLastThirtyDays = view.findViewById(R.id.dataLastThirtyDays);
    }

    @Override
    public void onResume() {
        super.onResume();
        Database db = ((MainActivity) requireActivity()).getDatabase();
        StatisticsEntity stats = db.queryStatistics();
        tvThisMonth.setText(stats.spentThisMonth / 100f + " €");
        tvLastThirtyDays.setText(stats.spentLastThirtyDays / 100f + " €");
    }
}
