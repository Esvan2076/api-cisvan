package com.cisvan.api.domain.searchhistory.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionDTO {
    
    private List<SearchHistoryDTO> suggestions;
    private String suggestionsType; // "user_history" o "trending"
}