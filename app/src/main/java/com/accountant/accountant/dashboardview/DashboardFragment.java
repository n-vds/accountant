package com.accountant.accountant.dashboardview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.accountant.accountant.R;

public class DashboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewPager view = (ViewPager) inflater.inflate(R.layout.fragment_dashboard, container, false);
        view.setAdapter(new MyPagerAdapter(getChildFragmentManager()));
        return view;
    }

    private static class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new DashboardOverviewFragment();
                case 1:
                    return new DashboardMonthlyFragment();
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? "Overview" : position == 1 ? "Monthly" : null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

}
