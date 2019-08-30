package com.accountant.accountant.db;

import java.util.*;

public class TagList {
    private List<Long> ids;
    private List<String> names;
    private Map<Long, String> nameById;

    public TagList() {
        nameById = new HashMap<>(0);
        names = new ArrayList<>(0);
        ids = new ArrayList<>(0);
    }

    public TagList(long[] tagIds, String[] tagNames) {
        ids = new ArrayList<>(tagIds.length);
        names = Arrays.asList(tagNames);
        nameById = new HashMap<>();

        for (int i = 0; i < tagIds.length; i++) {
            nameById.put(tagIds[i], tagNames[i]);
            ids.add(tagIds[i]);
        }
    }

    public String getName(long tagId) {
        return nameById.get(tagId);
    }

    public int getCount() {
        return nameById.size();
    }

    public List<Long> getIds() {
        return Collections.unmodifiableList(ids);
    }

    public List<String> getNames() {
        return Collections.unmodifiableList(names);
    }
}
