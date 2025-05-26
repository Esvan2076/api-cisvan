package com.cisvan.api.domain.searchhistory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.searchhistory.dtos.SearchHistoryDTO;
import com.cisvan.api.domain.searchhistory.dtos.SearchSuggestionDTO;
import com.cisvan.api.domain.searchtrending.SearchTrending;
import com.cisvan.api.domain.searchtrending.SearchTrendingRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {
    
    private final SearchHistoryRepository searchHistoryRepository;
    private final SearchTrendingRepository searchTrendingRepository;
    
    @Transactional
    public void recordSearch(Long userId, String searchTerm, String resultType, 
                           String resultId, String resultTitle) {
        SearchHistory history = SearchHistory.builder()
            .userId(userId)
            .searchTerm(searchTerm)
            .resultType(resultType)
            .resultId(resultId)
            .resultTitle(resultTitle)
            .build();
        
        searchHistoryRepository.save(history);
    }
    
    @Transactional(readOnly = true)
    public SearchSuggestionDTO getSuggestions(Long userId) {
        if (userId != null) {
            List<SearchHistory> userHistory = searchHistoryRepository
                .findTop10ByUserIdOrderByCreatedAtDesc(userId);
            
            if (!userHistory.isEmpty()) {
                // No necesitamos userSearchedIds aquí
                List<SearchHistoryDTO> suggestions = userHistory.stream()
                    .map(sh -> SearchHistoryDTO.builder()
                        .resultId(sh.getResultId())
                        .resultType(sh.getResultType())
                        .resultTitle(sh.getResultTitle())
                        .wasSearched(true)
                        .build())
                    .toList();
                
                System.out.println("User " + userId + " has " + suggestions.size() + " search suggestions");
                for (SearchHistoryDTO dto : suggestions) {
                    System.out.println("Suggestion: " + dto.getResultTitle() + " (" + dto.getResultId() + ")");
                }
                
                return SearchSuggestionDTO.builder()
                    .suggestions(suggestions)
                    .suggestionsType("user_history")
                    .build();
            } else {
                System.out.println("User " + userId + " has no search history");
            }
        } else {
            System.out.println("No user ID provided for search suggestions");
        }
        
        // Si no hay historial de usuario, devolver trending
        List<SearchTrending> trending = searchTrendingRepository
            .findTop10ByOrderByWeightedScoreDesc();
        
        List<SearchHistoryDTO> suggestions = trending.stream()
            .map(st -> SearchHistoryDTO.builder()
                .resultId(st.getResultId())
                .resultType(st.getResultType())
                .resultTitle(st.getResultTitle())
                .wasSearched(false)
                .build())
            .toList();
        
        System.out.println("Returning " + suggestions.size() + " trending suggestions");
        return SearchSuggestionDTO.builder()
            .suggestions(suggestions)
            .suggestionsType("trending")
            .build();
    }
    
    @Transactional
    public void updateTrendingSearches() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = now.minusWeeks(1);
        LocalDateTime twoWeeksAgo = now.minusWeeks(2);
        LocalDateTime twoMonthsAgo = now.minusMonths(2);
        LocalDateTime sixMonthsAgo = now.minusMonths(6);
        LocalDateTime oneYearAgo = now.minusYears(1);
        
        List<Object[]> trendingData = searchHistoryRepository.findTop10TrendingSearches(
            oneWeekAgo, twoWeeksAgo, twoMonthsAgo, sixMonthsAgo, oneYearAgo
        );
        
        // Limpiar tabla de trending
        searchTrendingRepository.deleteAll();
        
        // Insertar nuevos trending
        List<SearchTrending> newTrendings = trendingData.stream()
            .map(data -> SearchTrending.builder()
                .resultId((String) data[0])
                .resultType((String) data[1])
                .resultTitle((String) data[2])
                .searchCount(((Number) data[3]).longValue())
                .weightedScore(BigDecimal.valueOf(((Number) data[4]).doubleValue()))
                .build())
            .toList();
        
        searchTrendingRepository.saveAll(newTrendings);
    }
}