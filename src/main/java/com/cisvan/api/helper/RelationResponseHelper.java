package com.cisvan.api.helper;

import java.util.List;

import lombok.Data;

@Data
public class RelationResponseHelper {
    private String key;
    private List<Integer> values;

    public RelationResponseHelper() {
    }

    public RelationResponseHelper(String key, List<Integer> values) {
        this.key = key;
        this.values = values;
    }
}
