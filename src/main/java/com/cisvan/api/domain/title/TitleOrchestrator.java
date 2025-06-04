package com.cisvan.api.domain.title;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.akas.services.AkasLogicService;
import com.cisvan.api.domain.crew.CrewRepository;
import com.cisvan.api.domain.name.dto.NameEssencialDTO;
import com.cisvan.api.domain.name.dto.NameSearchResultDTO;
import com.cisvan.api.domain.name.services.NameLogicService;
import com.cisvan.api.domain.streaming.services.StreamingLogicService;
import com.cisvan.api.domain.title.dtos.TitleBasicDTO;
import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import com.cisvan.api.domain.title.dtos.TitleShowDTO;
import com.cisvan.api.domain.title.dtos.UnifiedSearchItemDTO;
import com.cisvan.api.domain.title.dtos.UnifiedSearchResultDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.MovieSearchResultDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.SerieSearchResultDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.TitleAdvancedSearchDTO;
import com.cisvan.api.domain.title.mappers.TitleMapper;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.title.services.TitleLogicService;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.title.services.UnifiedSearchService;
import com.cisvan.api.domain.titlerating.TitleRatingRepository;
import com.cisvan.api.domain.trending.Trending;
import com.cisvan.api.domain.trending.TrendingRepository;
import com.cisvan.api.domain.trending.TrendingService;
import com.cisvan.api.domain.trending.dtos.TrendingScoreDTO;
import com.cisvan.api.domain.userlist.UserListService;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleOrchestrator {

    private final CrewRepository crewRepository;
    private final TitleRatingRepository ratingRepository;
    private final TitleLogicService titleLogicService;
    private final TitleService titleService;
    private final NameLogicService nameLogicService;
    private final TitleMapper titleMapper;
    private final UserListService userListService;
    private final UserLogicService userLogicService;
    private final StreamingLogicService streamingLogicService;
    private final AkasLogicService akasLogicService;
    private final TrendingRepository trendingRepository;
    private final TrendingService trendingService;
    private final TitleRepository titleRepository;
    private final UnifiedSearchService unifiedSearchService;

    public Optional<TitleBasicDTO> getTitleBasicById(String tconst, HttpServletRequest request) {
        Optional<Title> titleOpt = titleService.getTitleById(tconst);
        if (titleOpt.isEmpty()) {
            return Optional.empty();
        }

        Title title = titleOpt.get();

        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (!userOpt.isEmpty()) {
            userOpt.ifPresent(user -> trendingService.registerVisitPoint(user.getId(), tconst));
        }

        // Ajustes si es episodio
        titleLogicService.adjustForEpisode(title, tconst);

        TitleBasicDTO detailDTO = titleMapper.toDTO(title);

        // Directores y escritores
        crewRepository.findById(tconst).ifPresent(titleCrew -> {
            List<String> directors = Optional.ofNullable(titleCrew.getDirectors()).orElse(Collections.emptyList());
            List<String> writers = Optional.ofNullable(titleCrew.getWriters()).orElse(Collections.emptyList());

            List<NameEssencialDTO> directos = nameLogicService.getNameBasicsDTOsByIds(
                    directors.stream().limit(3).toList());
            List<NameEssencialDTO> writersList = nameLogicService.getNameBasicsDTOsByIds(
                    writers.stream().limit(3).toList());

            detailDTO.setDirectors(directos);
            detailDTO.setWriters(writersList);
        });

        // Locale - idioma español
        Locale locale = LocaleContextHolder.getLocale();
        if ("es".equalsIgnoreCase(locale.getLanguage())) {
            akasLogicService.trySetSpanishTitle(detailDTO);
        }

        // Ratings
        ratingRepository.findById(tconst).ifPresent(detailDTO::setTitleRatings);

        // Streaming
        detailDTO.setStreamingServices(streamingLogicService.getStreamingServicesByTitle(tconst));

        // 🔥 Trending Score
        trendingRepository.findByContentId(tconst).ifPresent(trending -> {
            TrendingScoreDTO trendingScoreDTO = new TrendingScoreDTO(trending.getScore(),
                    trending.getHistoricalScore());
            detailDTO.setTrendingScore(trendingScoreDTO);
        });

        List<String> userTconstList = getUserTitleIds(request);

        if (userTconstList.isEmpty()) {
            detailDTO.setInUserList(false);
            return Optional.of(detailDTO);
        }

        Set<String> userTconstSet = new HashSet<>(userTconstList);

        if (userTconstSet.contains(tconst)) {
            detailDTO.setInUserList(true);
        } else {
            detailDTO.setInUserList(false);
        }

        return Optional.of(detailDTO);
    }

    public List<Object> searchEverything(String query) {
        List<Object> results = new ArrayList<>();

        results.addAll(titleLogicService.searchMovies(query));
        results.addAll(titleLogicService.searchSeries(query));
        results.addAll(nameLogicService.searchNames(query));

        return results.stream().limit(10).toList();
    }

    public Page<TitleKnownForDTO> searchAdvancedTitles(TitleAdvancedSearchDTO criteria, int page) {
        Pageable pageable = PageRequest.of(page, 20); // Tamaño fijo de 20 como se solicitó
        return titleService.advancedSearch(criteria, pageable);
    }

    public List<TitleShowDTO> getListOfUser(HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return Collections.emptyList();
        }
        Users user = userOpt.get();

        return titleLogicService.getTitlesForUser(user);
    }

    public List<TitleShowDTO> getTop20Series(HttpServletRequest request) {
        List<String> userTconstList = getUserTitleIds(request);
        List<TitleShowDTO> series = titleService.getTop20Series();

        Set<String> userTconstSet = new HashSet<>(userTconstList);

        for (TitleShowDTO title : series) {
            if (userTconstSet.contains(title.getTconst())) {
                title.setInUserList(true);
            }
        }

        return series;
    }

    public List<TitleShowDTO> getTop20NonSeries(HttpServletRequest request) {
        List<String> userTconstList = getUserTitleIds(request);
        List<TitleShowDTO> nonSeries = titleService.getTop20NonSeries();

        Set<String> userTconstSet = new HashSet<>(userTconstList);

        for (TitleShowDTO title : nonSeries) {
            if (userTconstSet.contains(title.getTconst())) {
                title.setInUserList(true);
            }
        }

        return nonSeries;
    }

    public List<TitleShowDTO> getTop20Trending(HttpServletRequest request) {
        List<String> userTconstList = getUserTitleIds(request);
        Set<String> userTconstSet = new HashSet<>(userTconstList);

        // 1. Obtener trending activos con score > 0
        List<Trending> trendingCandidates = trendingRepository.findAllByScoreGreaterThan(0);

        List<TitleShowDTO> result = new ArrayList<>();
        Set<String> includedTconsts = new HashSet<>();

        // 2. Convertir a DTO y marcar en lista del usuario
        for (Trending t : trendingCandidates) {
            titleService.getTitleShowDTOById(t.getContentId()).ifPresent(dto -> {
                dto.setInUserList(userTconstSet.contains(dto.getTconst()));
                result.add(dto);
                includedTconsts.add(dto.getTconst());
            });
        }

        // 3. Rellenar con títulos más votados (excluyendo episodios y los ya añadidos)
        int remaining = 20 - result.size();
        if (remaining > 0) {
            List<Object[]> fallback = titleRepository.findFallbackTitlesForTrending(includedTconsts, remaining);
            for (Object[] row : fallback) {
                String tconst = (String) row[0];
                titleService.getTitleShowDTOById(tconst).ifPresent(dto -> {
                    dto.setInUserList(userTconstSet.contains(dto.getTconst()));
                    result.add(dto);
                });
            }
        }

        return result;
    }

    public List<TitleShowDTO> getFinalRecommendations(HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Users user = userOpt.get();

        return titleLogicService.getRecommendedTitlesForUser(user);
    }

    public List<TitleShowDTO> getFinalRecommendationsByUserId(Long userId) {
        return titleLogicService.getRecommendedTitlesForUserId(userId);
    }

    public List<String> getUserTitleIds(HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return Collections.emptyList();
        }
        Users user = userOpt.get();
        return userListService.getTitleIdsByUserId(user.getId());
    }

    // Agregar este método después de searchEverything
    public UnifiedSearchResultDTO unifiedSearch(String query, Long userId, String filter) {
        boolean hasQuery = query != null && !query.trim().isEmpty();
        List<UnifiedSearchItemDTO> items = new ArrayList<>();

        if (!hasQuery) {
            items = handleEmptyQuerySearch(userId, filter);
        } else {
            items = handleQuerySearch(query, userId, filter);
        }
        
        return UnifiedSearchResultDTO.builder()
                .items(items)
                .hasQuery(hasQuery)
                .build();
    }

    /**
     * Maneja búsquedas cuando no hay query (sugerencias)
     */
    private List<UnifiedSearchItemDTO> handleEmptyQuerySearch(Long userId, String filter) {
        List<UnifiedSearchItemDTO> items = new ArrayList<>();
        Set<String> addedIds = new HashSet<>();

        // 1. Obtener búsquedas recientes del usuario
        if (userId != null) {
            items.addAll(unifiedSearchService.getUserRecentSearches(userId, filter, addedIds));
        }

        // 2. Completar con contenido popular
        int remaining = 10 - items.size();
        if (remaining > 0) {
            items.addAll(unifiedSearchService.getPopularContent(filter, remaining, addedIds));
        }

        return items;
    }

    /**
     * Maneja búsquedas con query
     */
    private List<UnifiedSearchItemDTO> handleQuerySearch(String query, Long userId, String filter) {
        List<UnifiedSearchItemDTO> items = new ArrayList<>();
        
        // Obtener IDs para marcar como reciente/popular
        Set<String> userRecentIds = unifiedSearchService.getUserRecentIds(userId, filter);
        Set<String> popularIds = unifiedSearchService.getPopularIds(filter);

        // Buscar según el filtro
        if (filter.equals("all") || filter.equals("movie")) {
            items.addAll(searchMoviesWithMarkers(query, userRecentIds, popularIds));
        }

        if (filter.equals("all") || filter.equals("serie")) {
            items.addAll(searchSeriesWithMarkers(query, userRecentIds, popularIds));
        }

        if (filter.equals("all") || filter.equals("person")) {
            items.addAll(searchPeopleWithMarkers(query, userRecentIds, popularIds));
        }

        // Ordenar por prioridad y limitar a 10
        return items.stream()
                .sorted(Comparator.comparingInt(UnifiedSearchItemDTO::getPriority))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * Busca películas con marcadores
     */
    private List<UnifiedSearchItemDTO> searchMoviesWithMarkers(String query, Set<String> recentIds, Set<String> popularIds) {
        return titleLogicService.searchMovies(query).stream()
                .map(movie -> {
                    boolean isRecent = recentIds.contains(movie.getTconst());
                    boolean isPopular = popularIds.contains(movie.getTconst());
                    
                    return UnifiedSearchItemDTO.builder()
                            .id(movie.getTconst())
                            .type("movie")
                            .title(movie.getPrimaryTitle())
                            .subtitle(buildMovieSubtitle(movie))
                            .isRecent(isRecent)
                            .isPopular(isPopular && !isRecent)
                            .priority(isRecent ? 1 : (isPopular ? 2 : 3))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca series con marcadores
     */
    private List<UnifiedSearchItemDTO> searchSeriesWithMarkers(String query, Set<String> recentIds, Set<String> popularIds) {
        return titleLogicService.searchSeries(query).stream()
                .map(serie -> {
                    boolean isRecent = recentIds.contains(serie.getTconst());
                    boolean isPopular = popularIds.contains(serie.getTconst());
                    
                    return UnifiedSearchItemDTO.builder()
                            .id(serie.getTconst())
                            .type("serie")
                            .title(serie.getPrimaryTitle())
                            .subtitle(buildSerieSubtitle(serie))
                            .isRecent(isRecent)
                            .isPopular(isPopular && !isRecent)
                            .priority(isRecent ? 1 : (isPopular ? 2 : 3))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca personas con marcadores
     */
    private List<UnifiedSearchItemDTO> searchPeopleWithMarkers(String query, Set<String> recentIds, Set<String> popularIds) {
        return nameLogicService.searchNames(query).stream()
                .map(name -> {
                    boolean isRecent = recentIds.contains(name.getNconst());
                    boolean isPopular = popularIds.contains(name.getNconst());
                    
                    return UnifiedSearchItemDTO.builder()
                            .id(name.getNconst())
                            .type("person")
                            .title(name.getPrimaryName())
                            .subtitle(buildPersonSubtitle(name))
                            .isRecent(isRecent)
                            .isPopular(isPopular && !isRecent)
                            .priority(isRecent ? 1 : (isPopular ? 2 : 3))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Construye subtítulo para películas
     */
    private String buildMovieSubtitle(MovieSearchResultDTO movie) {
        String subtitle = String.valueOf(movie.getStartYear());
        if (movie.getActors() != null && !movie.getActors().isEmpty()) {
            subtitle += " — " + movie.getActors();
        }
        return subtitle;
    }

    /**
     * Construye subtítulo para series
     */
    private String buildSerieSubtitle(SerieSearchResultDTO serie) {
        String subtitle = String.valueOf(serie.getStartYear());
        if (serie.getEndYear() != null) {
            subtitle += " - " + serie.getEndYear();
        }
        if (serie.getActors() != null && !serie.getActors().isEmpty()) {
            subtitle += " — " + serie.getActors();
        }
        return subtitle;
    }

    private String buildPersonSubtitle(NameSearchResultDTO name) {
        StringBuilder subtitle = new StringBuilder();

        if (name.getPrimaryProfession() != null && !name.getPrimaryProfession().isEmpty()) {
            subtitle.append(name.getPrimaryProfession());
        }

        if (name.getPrincipalTitle() != null && name.getPrincipalTitle().getPrimaryTitle() != null) {
            if (subtitle.length() > 0)
                subtitle.append(", ");
            subtitle.append(name.getPrincipalTitle().getPrimaryTitle())
                    .append(" (").append(name.getPrincipalTitle().getStartYear());

            if (name.getPrincipalTitle().getEndYear() != null) {
                subtitle.append(" - ").append(name.getPrincipalTitle().getEndYear());
            }
            subtitle.append(")");
        }

        return subtitle.toString();
    }
}
