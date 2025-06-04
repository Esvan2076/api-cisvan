package com.cisvan.api.domain.searchhistory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.searchhistory.dtos.SearchHistoryDTO;
import com.cisvan.api.domain.searchhistory.dtos.SearchSuggestionDTO;
import com.cisvan.api.domain.searchtrending.SearchTrending;
import com.cisvan.api.domain.searchtrending.SearchTrendingRepository;
import com.cisvan.api.domain.title.repos.TitleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final SearchTrendingRepository searchTrendingRepository;
    private final TitleRepository titleRepository;

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
        List<SearchHistoryDTO> suggestions = new ArrayList<>();
        Set<String> existingIds = new HashSet<>();

        if (userId != null) {
            List<SearchHistory> userHistory = searchHistoryRepository
                    .findTop10ByUserIdOrderByCreatedAtDesc(userId);

            for (SearchHistory sh : userHistory) {
                suggestions.add(SearchHistoryDTO.builder()
                        .resultId(sh.getResultId())
                        .resultType(sh.getResultType())
                        .resultTitle(sh.getResultTitle())
                        .wasSearched(true)
                        .build());
                existingIds.add(sh.getResultId());
            }

            int remaining = 10 - suggestions.size();

            if (remaining > 0) {
                List<SearchTrending> trending = searchTrendingRepository
                        .findTop10ByOrderByWeightedScoreDesc();

                for (SearchTrending st : trending) {
                    if (existingIds.contains(st.getResultId())) continue;
                    suggestions.add(SearchHistoryDTO.builder()
                            .resultId(st.getResultId())
                            .resultType(st.getResultType())
                            .resultTitle(st.getResultTitle())
                            .wasSearched(false)
                            .build());
                    existingIds.add(st.getResultId());
                    if (suggestions.size() >= 10) break;
                }

                remaining = 10 - suggestions.size(); // Recalcular por si aún faltan
            }

            if (remaining > 0) {
                List<Object[]> fallback = titleRepository.findTopRatedTitlesExcluding(existingIds, remaining);
                for (Object[] row : fallback) {
                    String tconst = (String) row[0];
                    String title = (String) row[1];
                    String titleType = (String) row[2];

                    String resultType = (titleType.equals("tvSeries") || titleType.equals("tvMiniSeries"))
                            ? "serie" : "movie";

                    suggestions.add(SearchHistoryDTO.builder()
                            .resultId(tconst)
                            .resultType(resultType)
                            .resultTitle(title)
                            .wasSearched(false)
                            .build());
                }
            }

            return SearchSuggestionDTO.builder()
                    .suggestions(suggestions)
                    .suggestionsType("user_history")
                    .build();
        }

        // Usuario no autenticado
        List<SearchTrending> trending = searchTrendingRepository
                .findTop10ByOrderByWeightedScoreDesc();

        for (SearchTrending st : trending) {
            suggestions.add(SearchHistoryDTO.builder()
                    .resultId(st.getResultId())
                    .resultType(st.getResultType())
                    .resultTitle(st.getResultTitle())
                    .wasSearched(false)
                    .build());
            existingIds.add(st.getResultId());
        }

        int remaining = 10 - suggestions.size();

        if (remaining > 0) {
            List<Object[]> fallback = titleRepository.findTopRatedTitlesExcluding(existingIds, remaining);
            for (Object[] row : fallback) {
                String tconst = (String) row[0];
                String title = (String) row[1];
                String titleType = (String) row[2];

                String resultType = (titleType.equals("tvSeries") || titleType.equals("tvMiniSeries"))
                        ? "serie" : "movie";

                suggestions.add(SearchHistoryDTO.builder()
                        .resultId(tconst)
                        .resultType(resultType)
                        .resultTitle(title)
                        .wasSearched(false)
                        .build());
            }
        }

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