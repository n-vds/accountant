package com.accountant.accountant.db;

import java.util.Date;

public class SpendingEntity {
    public final long id;
    public final long timestamp;
    public final int amount;
    public long[] tagIds;
    public String[] tagNames;

    public SpendingEntity(long id, long timestamp, int amount, long[] tags, String[] tagNames) {
        this.id = id;
        this.timestamp = timestamp;
        this.amount = amount;
        this.tagIds = tags;
        this.tagNames = tagNames;
    }
}
