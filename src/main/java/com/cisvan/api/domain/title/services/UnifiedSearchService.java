package com.cisvan.api.domain.title.services;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.repos.NameRepository;
import com.cisvan.api.domain.namerating.NameRating;
import com.cisvan.api.domain.namerating.NameRatingRepository;
import com.cisvan.api.domain.searchhistory.SearchHistory;
import com.cisvan.api.domain.searchhistory.SearchHistoryRepository;
import com.cisvan.api.domain.searchtrending.SearchTrending;
import com.cisvan.api.domain.searchtrending.SearchTrendingRepository;
import com.cisvan.api.domain.title.dtos.UnifiedSearchItemDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedSearchService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final SearchTrendingRepository searchTrendingRepository;
    private final NameRatingRepository nameRatingRepository;
    private final TitleRepository titleRepository;
    private final NameRepository nameRepository;

    /**
     * Obtiene búsquedas recientes del usuario filtradas por tipo
     */
    public List<UnifiedSearchItemDTO> getUserRecentSearches(Long userId, String filter, Set<String> excludeIds) {
        if (userId == null) {
            return Collections.emptyList();
        }

        System.err.println("Fetching recent searches for user: " + userId + " with filter: " + filter);

        List<String> types = mapFilterToTypes(filter);
        List<SearchHistory> userHistory = searchHistoryRepository
                .findTop10ByUserIdAndTypeOrderByCreatedAtDesc(userId, types);

        System.err.println("Registros crudos traídos de DB (userHistory): " + userHistory.size());

        return userHistory.stream()
                .filter(history -> !excludeIds.contains(history.getResultId()))
                .map(history -> {
                    excludeIds.add(history.getResultId());
                    return UnifiedSearchItemDTO.builder()
                            .id(history.getResultId())
                            .type(normalizeType(history.getResultType()))
                            .title(history.getResultTitle())
                            .subtitle(getSubtitleForId(history.getResultType(), history.getResultId()))
                            .isRecent(true)
                            .isPopular(false)
                            .priority(1)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene contenido popular según el filtro
     */
    public List<UnifiedSearchItemDTO> getPopularContent(String filter, int limit, Set<String> excludeIds) {
        List<UnifiedSearchItemDTO> items = new ArrayList<>();
        
        if (filter.equals("person")) {
            // Para personas, usar name_ratings ordenado por num_votes
            List<NameRating> popularPeople = nameRatingRepository
                    .findTop100ByOrderByNumVotesDesc();
            
            items = popularPeople.stream()
                    .filter(rating -> !excludeIds.contains(rating.getNconst()))
                    .limit(limit)
                    .map(rating -> {
                        excludeIds.add(rating.getNconst());
                        Name name = nameRepository.findById(rating.getNconst()).orElse(null);
                        if (name == null) return null;
                        
                        return UnifiedSearchItemDTO.builder()
                                .id(rating.getNconst())
                                .type("person")
                                .title(name.getPrimaryName())
                                .subtitle(getPersonSubtitle(name))
                                .isRecent(false)
                                .isPopular(true)
                                .priority(2)
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            // Para movies y series, usar search_trending
            List<String> types = mapFilterToTypes(filter);
            List<SearchTrending> trending = searchTrendingRepository
                    .findTopByTypeOrderByWeightedScoreDesc(types, limit * 2); // Pedir más por si hay excludeIds
            
            items = trending.stream()
                    .filter(t -> !excludeIds.contains(t.getResultId()))
                    .limit(limit)
                    .map(trend -> {
                        excludeIds.add(trend.getResultId());
                        return UnifiedSearchItemDTO.builder()
                                .id(trend.getResultId())
                                .type(normalizeType(trend.getResultType()))
                                .title(trend.getResultTitle())
                                .subtitle(getSubtitleForId(trend.getResultType(), trend.getResultId()))
                                .isRecent(false)
                                .isPopular(true)
                                .priority(2)
                                .build();
                    })
                    .collect(Collectors.toList());
        }
        
        return items;
    }

    /**
     * Obtiene IDs de búsquedas recientes del usuario por tipo
     */
    public Set<String> getUserRecentIds(Long userId, String filter) {
        if (userId == null) {
            return Collections.emptySet();
        }
        
        List<String> types = mapFilterToTypes(filter);
        return searchHistoryRepository
                .findTop10ByUserIdAndTypeOrderByCreatedAtDesc(userId, types)
                .stream()
                .map(SearchHistory::getResultId)
                .collect(Collectors.toSet());
    }

    /**
     * Obtiene IDs de contenido popular por tipo
     */
    public Set<String> getPopularIds(String filter) {
        if (filter.equals("person")) {
            return nameRatingRepository
                    .findTop100ByOrderByNumVotesDesc()
                    .stream()
                    .map(NameRating::getNconst)
                    .collect(Collectors.toSet());
        } else {
            List<String> types = mapFilterToTypes(filter);
            return searchTrendingRepository
                    .findTopByTypeOrderByWeightedScoreDesc(types, 100)
                    .stream()
                    .map(SearchTrending::getResultId)
                    .collect(Collectors.toSet());
        }
    }

    /**
     * Mapea el filtro a tipos de base de datos
     */
    public List<String> mapFilterToTypes(String filter) {
        switch (filter) {
            case "movie":
                return List.of("movie");
            case "serie":
                return List.of("tvSeries", "tvMiniSeries");
            case "person":
                return List.of("person");
            case "all":
            default:
                return List.of("movie", "tvSeries", "tvMiniSeries", "person");
        }
    }

    /**
     * Normaliza el tipo para el frontend
     */
    private String normalizeType(String dbType) {
        if ("tvSeries".equals(dbType) || "tvMiniSeries".equals(dbType)) {
            return "serie";
        }
        return dbType;
    }

    /**
     * Obtiene subtítulo según el tipo y ID
     */
    private String getSubtitleForId(String type, String id) {
        try {
            if ("movie".equals(type)) {
                return titleRepository.findById(id)
                        .map(title -> String.valueOf(title.getStartYear()))
                        .orElse("");
            } else if ("tvSeries".equals(type) || "tvMiniSeries".equals(type)) {
                return titleRepository.findById(id)
                        .map(title -> {
                            String subtitle = String.valueOf(title.getStartYear());
                            if (title.getEndYear() != null) {
                                subtitle += " - " + title.getEndYear();
                            }
                            return subtitle;
                        })
                        .orElse("");
            } else if ("person".equals(type)) {
                return nameRepository.findById(id)
                        .map(this::getPersonSubtitle)
                        .orElse("");
            }
        } catch (Exception e) {
            log.warn("Error getting subtitle for {} ({}): {}", id, type, e.getMessage());
        }
        return "";
    }

    /**
     * Construye subtítulo para personas
     */
    private String getPersonSubtitle(Name name) {
        if (name.getPrimaryProfession() != null && !name.getPrimaryProfession().isEmpty()) {
            return name.getPrimaryProfession().stream()
                    .limit(3)
                    .collect(Collectors.joining(", "));
        }
        return "";
    }
}