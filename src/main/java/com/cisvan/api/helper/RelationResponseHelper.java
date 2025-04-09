package com.cisvan.api.helper;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelationResponseHelper {
    
    private String key;
    private List<Integer> values;
}