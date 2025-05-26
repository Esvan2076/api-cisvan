package com.cisvan.api.domain.searchhistory.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryDTO {

    private String resultId;
    private String resultType;
    private String resultTitle;
    private boolean wasSearched;
}