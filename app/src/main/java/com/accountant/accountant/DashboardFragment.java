package com.accountant.accountant;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.StatisticsEntity;
import org.w3c.dom.Text;

public class DashboardFragment extends Fragment {
    private static final String[] MONTH_NAMES = new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private static final int[] MONTH_IDS = new int[]{
            R.id.january, R.id.february, R.id.march, R.id.april, R.id.may, R.id.june,
            R.id.july, R.id.august, R.id.september, R.id.october, R.id.november, R.id.december
    };
    private TextView tvThisMonth;
    private TextView tvLastThirtyDays;
    private TextView tvMonthlyAvg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ConstraintLayout root = (ConstraintLayout) inflater.inflate(R.layout.fragment_dashboard, container, false);

        ConstraintSet constraints = new ConstraintSet();

        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        int[] tvNamesIds = new int[12];

        for (int month = 0; month < 12; month++) {
            TextView tvName = new TextView(requireContext());
            tvName.setId(View.generateViewId());
            tvName.setText(MONTH_NAMES[month] + ":");
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            root.addView(tvName);
            tvNamesIds[month] = tvName.getId();

            TextView tvData = new TextView(requireContext());
            tvData.setId(MONTH_IDS[month]);
            tvData.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            root.addView(tvData);
        }

        constraints.clone(root);

        for (int month = 0; month < 12; month++) {
            constraints.connect(tvNamesIds[month], ConstraintSet.START, R.id.tvMonthlyAvg, ConstraintSet.START);
            constraints.connect(MONTH_IDS[month], ConstraintSet.START, tvNamesIds[month], ConstraintSet.END);
            if (month == 0) {
                constraints.connect(tvNamesIds[month], ConstraintSet.TOP, R.id.tvMonthlyAvg, ConstraintSet.BOTTOM);
                constraints.connect(MONTH_IDS[month], ConstraintSet.TOP, R.id.dataMonthlyAvg, ConstraintSet.BOTTOM);
            } else {
                constraints.connect(tvNamesIds[month], ConstraintSet.TOP, MONTH_IDS[month - 1], ConstraintSet.BOTTOM);
                constraints.connect(MONTH_IDS[month], ConstraintSet.TOP, MONTH_IDS[month - 1], ConstraintSet.BOTTOM);
            }
        }
        constraints.applyTo(root);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        tvThisMonth = view.findViewById(R.id.dataThisMonth);
        tvLastThirtyDays = view.findViewById(R.id.dataLastThirtyDays);
        tvMonthlyAvg = view.findViewById(R.id.dataMonthlyAvg);
    }

    @Override
    public void onResume() {
        super.onResume();
        Database db = ((MainActivity) requireActivity()).getDatabase();
        StatisticsEntity stats = db.queryStatistics();
        tvThisMonth.setText(stats.spentThisMonth / 100f + " €");
        tvLastThirtyDays.setText(stats.spentLastThirtyDays / 100f + " €");
        tvMonthlyAvg.setText(stats.spendingAvgMonth / 100f + " €");
        for (int month = 0; month < 12; month++) {
            TextView tv = getView().findViewById(MONTH_IDS[month]);
            float avg = stats.spendingAvgMonthByMonth[month];
            if (Float.isNaN(avg)) {
                tv.setText("?");
            } else {
                tv.setText(avg / 100f + " €");
            }
        }
    }
}
