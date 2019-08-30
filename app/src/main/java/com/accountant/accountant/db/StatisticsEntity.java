package com.accountant.accountant.db;

import java.util.List;

public class StatisticsEntity {
    public final int spentThisMonth;
    public final int spentLastThirtyDays;
    public final float spendingAvgMonth;
    public final float[] spendingAvgMonthByMonth;
    public final List<SpendingByTagEntry> spendingByTag;

    public StatisticsEntity(int spentThisMonth, int spentLastThirtyDays, float spendingAvgMonth,
                            float[] spendingAvgMonthByMonth, List<SpendingByTagEntry> spendingByTag) {
        this.spentThisMonth = spentThisMonth;
        this.spentLastThirtyDays = spentLastThirtyDays;
        this.spendingAvgMonth = spendingAvgMonth;
        this.spendingAvgMonthByMonth = spendingAvgMonthByMonth;
        this.spendingByTag = spendingByTag;
    }


    public static final class SpendingByTagEntry {
        public final long id;
        public final String name;
        public final int amount;
        public final float percentage;

        public SpendingByTagEntry(long id, String name, int amount, float percentage) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.percentage = percentage;
        }
    }
}
