package com.cisvan.api.domain.title.dtos;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UnifiedSearchResultDTO {
    private List<UnifiedSearchItemDTO> items;
    private boolean hasQuery;
}

