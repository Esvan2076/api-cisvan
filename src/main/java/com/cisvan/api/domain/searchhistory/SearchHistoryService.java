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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
        
        // Verificar si ya existe una búsqueda para este usuario y resultado
        Optional<SearchHistory> existingSearch = searchHistoryRepository
            .findByUserIdAndResultId(userId, resultId);
        
        if (existingSearch.isPresent()) {
            // Si ya existe, actualizar el timestamp y el término de búsqueda
            SearchHistory history = existingSearch.get();
            history.setSearchTerm(searchTerm);
            history.setCreatedAt(LocalDateTime.now()); // Actualizar timestamp
            searchHistoryRepository.save(history);
        } else {
            // Si no existe, crear una nueva entrada
            SearchHistory history = SearchHistory.builder()
                .userId(userId)
                .searchTerm(searchTerm)
                .resultType(resultType)
                .resultId(resultId)
                .resultTitle(resultTitle)
                .build();
            
            searchHistoryRepository.save(history);
        }
    }

    @Transactional(readOnly = true)
    public SearchSuggestionDTO getSuggestions(Long userId) {
        // Si hay usuario, intentar obtener su historial
        if (userId != null) {
            List<SearchHistory> userHistory = searchHistoryRepository
                    .findTop10ByUserIdOrderByCreatedAtDesc(userId);

            if (!userHistory.isEmpty()) {
                List<SearchHistoryDTO> suggestions = userHistory.stream()
                        .map(sh -> SearchHistoryDTO.builder()
                                .resultId(sh.getResultId())
                                .resultType(sh.getResultType())
                                .resultTitle(sh.getResultTitle())
                                .wasSearched(true)
                                .build())
                        .toList();

                return SearchSuggestionDTO.builder()
                        .suggestions(suggestions)
                        .suggestionsType("user_history")
                        .build();
            }
        }

        // Si no hay usuario o no tiene historial, devolver trending
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

        return SearchSuggestionDTO.builder()
                .suggestions(suggestions)
                .suggestionsType("trending")
                .build();
    }

    @Transactional(readOnly = true)
    public Set<String> getUserRecentSearchIds(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        List<SearchHistory> userHistory = searchHistoryRepository
                .findTop10ByUserIdOrderByCreatedAtDesc(userId);

        return userHistory.stream()
                .map(SearchHistory::getResultId)
                .collect(Collectors.toSet());
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
                oneWeekAgo, twoWeeksAgo, twoMonthsAgo, sixMonthsAgo, oneYearAgo);
        
    //  @Query("""
    //     SELECT sh.resultId, sh.resultType, sh.resultTitle, COUNT(sh) as count,
    //            SUM(CASE 
    //                WHEN sh.createdAt > :oneWeekAgo THEN 1.25
    //                WHEN sh.createdAt > :twoWeeksAgo THEN 1.00
    //                WHEN sh.createdAt > :twoMonthsAgo THEN 0.75
    //                WHEN sh.createdAt > :sixMonthsAgo THEN 0.50
    //                ELSE 0.25
    //            END) as weightedScore
    //     FROM SearchHistory sh
    //     WHERE sh.createdAt > :oneYearAgo
    //     GROUP BY sh.resultId, sh.resultType, sh.resultTitle
    //     ORDER BY weightedScore DESC
    //     LIMIT 10
    // """)
    // List<Object[]> findTop10TrendingSearches(
    //     @Param("oneWeekAgo") LocalDateTime oneWeekAgo,
    //     @Param("twoWeeksAgo") LocalDateTime twoWeeksAgo,
    //     @Param("twoMonthsAgo") LocalDateTime twoMonthsAgo,
    //     @Param("sixMonthsAgo") LocalDateTime sixMonthsAgo,
    //     @Param("oneYearAgo") LocalDateTime oneYearAgo
    // );

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

    @Transactional(readOnly = true)
    public Set<String> getPopularSearchIds() {
        List<SearchTrending> trending = searchTrendingRepository
                .findTop10ByOrderByWeightedScoreDesc();

        System.out.println("Trending found: " + trending.size());
        trending.forEach(t -> System.out.println(">> " + t.getResultId()));

        return trending.stream()
                .map(SearchTrending::getResultId)
                .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<String> getUserRecentSearchIdsByType(Long userId, List<String> types) {
        if (userId == null) {
            return Collections.emptySet();
        }
        
        List<SearchHistory> userHistory = searchHistoryRepository
            .findTop10ByUserIdAndTypeOrderByCreatedAtDesc(userId, types);
        
        return userHistory.stream()
            .map(SearchHistory::getResultId)
            .collect(Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Set<String> getPopularSearchIdsByType(List<String> types) {
        List<SearchTrending> trending = searchTrendingRepository
            .findTop10ByTypeOrderByWeightedScoreDesc(types);
        
        return trending.stream()
            .map(SearchTrending::getResultId)
            .collect(Collectors.toSet());
    }
}