package com.accountant.accountant.db;

public class SpendingEntity {
    public final long id;
    public final long timestamp;
    public final int amount;
    public Long tagId;
    public String tagName;

    public SpendingEntity(long id, long timestamp, int amount, Long tagId, String tagName) {
        this.id = id;
        this.timestamp = timestamp;
        this.amount = amount;
        this.tagId = tagId;
        this.tagName = tagName;
    }
}
