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

public class DashboardOverviewFragment extends Fragment {
    private TextView tvThisMonth;
    private TextView tvLastThirtyDays;
    private TextView tvMonthlyAvg;
    private TextView tvSpendingByTag;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvThisMonth = view.findViewById(R.id.dataThisMonth);
        tvLastThirtyDays = view.findViewById(R.id.dataLastThirtyDays);
        tvMonthlyAvg = view.findViewById(R.id.dataMonthlyAvg);
        tvSpendingByTag = view.findViewById(R.id.tvSpendingByTag);
    }

    @Override
    public void onResume() {
        super.onResume();
        Database db = ((MainActivity) requireActivity()).getDatabase();
        StatisticsEntity stats = db.queryStatistics();
        tvThisMonth.setText(Math.round(stats.spentThisMonth / 100f) + " €");
        tvLastThirtyDays.setText(Math.round(stats.spentLastThirtyDays / 100f) + " €");
        tvMonthlyAvg.setText(Math.round(stats.spendingAvgMonth / 100f) + " €");
        StringBuilder byTagString = new StringBuilder();
        for (StatisticsEntity.SpendingByTagEntry entry : stats.spendingByTag) {
            byTagString.append(entry.name == null ? "<No tag>" : entry.name).append(": ")
                    .append(Math.round(entry.amount / 100f)).append(" €")
                    .append(" (").append(Math.round(entry.percentage * 100)).append("%)")
                    .append('\n');
        }
        tvSpendingByTag.setText(byTagString.toString());
    }
}
