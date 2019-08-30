package com.accountant.accountant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.accountant.accountant.db.Database;
import com.accountant.accountant.db.StatisticsEntity;

public class DashboardMonthlyFragment extends Fragment {
    private static final String[] MONTH_NAMES = new String[]{
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    private RecyclerView vMonthData;
    private MyAdapter monthDataAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard_monthly, container, false);

        vMonthData = root.findViewById(R.id.listMonthData);
        vMonthData.setHasFixedSize(true);
        vMonthData.setLayoutManager(new LinearLayoutManager(requireContext()));
        monthDataAdapter = new MyAdapter();
        vMonthData.setAdapter(monthDataAdapter);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Database db = ((MainActivity) requireActivity()).getDatabase();
        StatisticsEntity stats = db.queryStatistics();
        monthDataAdapter.setData(stats.spendingAvgMonthByMonth);
        monthDataAdapter.notifyDataSetChanged();
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        private float[] data = null;

        void setData(float[] data) {
            this.data = data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dashboard_month_spent_row, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.monthName.setText(MONTH_NAMES[position]);
            if (data == null || Float.isNaN(data[position])) {
                holder.amount.setText("?");
            } else {
                holder.amount.setText(String.format("%d €", Math.round(data[position] / 100f)));
            }
        }

        @Override
        public int getItemCount() {
            return 12;
        }

        private static class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView monthName;
            private TextView amount;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                monthName = itemView.findViewById(R.id.monthName);
                amount = itemView.findViewById(R.id.tvAmount);
            }
        }

    }
}
