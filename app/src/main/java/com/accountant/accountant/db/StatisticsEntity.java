package com.accountant.accountant.db;

public class StatisticsEntity {
    public final int spentThisMonth;
    public final int spentLastThirtyDays;
    public final float spendingAvgMonth;
    public final float[] spendingAvgMonthByMonth;

    public StatisticsEntity(int spentThisMonth, int spentLastThirtyDays, float spendingAvgMonth, float[] spendingAvgMonthByMonth) {
        this.spentThisMonth = spentThisMonth;
        this.spentLastThirtyDays = spentLastThirtyDays;
        this.spendingAvgMonth = spendingAvgMonth;
        this.spendingAvgMonthByMonth = spendingAvgMonthByMonth;
    }
}
