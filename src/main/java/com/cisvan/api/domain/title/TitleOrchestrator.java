package com.cisvan.api.domain.title;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.dto.NameEssencialDTO;
import com.cisvan.api.domain.name.dto.NameSearchResultDTO;
import com.cisvan.api.domain.name.repos.NameRepository;
import com.cisvan.api.domain.name.services.NameLogicService;
import com.cisvan.api.domain.principal.Principal;
import com.cisvan.api.domain.principal.repos.PrincipalRepository;
import com.cisvan.api.domain.searchhistory.SearchHistory;
import com.cisvan.api.domain.searchhistory.SearchHistoryRepository;
import com.cisvan.api.domain.searchhistory.SearchHistoryService;
import com.cisvan.api.domain.searchtrending.SearchTrending;
import com.cisvan.api.domain.searchtrending.SearchTrendingRepository;
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
    private final SearchHistoryService searchHistoryService;
    private final TrendingRepository trendingRepository;
    private final TrendingService trendingService;
    private final SearchHistoryRepository searchHistoryRepository;
    private final SearchTrendingRepository searchTrendingRepository;
    private final TitleRepository titleRepository;
    private final NameRepository nameRepository;
    private final PrincipalRepository principalRepository;

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
            // Caso de input vacío: combinar historial + tendencias con filtro
            Set<String> addedIds = new HashSet<>();

            if (userId != null) {
                // Obtener historial filtrado por tipo
                List<SearchHistory> userHistory = searchHistoryRepository
                        .findTop10ByUserIdAndTypeOrderByCreatedAtDesc(userId, mapFilterToTypes(filter));

                for (SearchHistory history : userHistory) {
                    items.add(UnifiedSearchItemDTO.builder()
                            .id(history.getResultId())
                            .type(history.getResultType())
                            .title(history.getResultTitle())
                            .subtitle(getSubtitleForSuggestion(history.getResultType(), history.getResultId()))
                            .isRecent(true)
                            .isPopular(false)
                            .priority(1)
                            .build());

                    addedIds.add(history.getResultId());
                }

                int remaining = 10 - userHistory.size();
                if (remaining > 0) {
                    // Obtener tendencias filtradas por tipo
                    List<SearchTrending> trending = searchTrendingRepository
                            .findTop20ByTypeOrderByWeightedScoreDesc(mapFilterToTypes(filter));

                    List<SearchTrending> filteredTrending = trending.stream()
                            .filter(t -> !addedIds.contains(t.getResultId()))
                            .limit(remaining)
                            .collect(Collectors.toList());

                    for (SearchTrending trend : filteredTrending) {
                        items.add(UnifiedSearchItemDTO.builder()
                                .id(trend.getResultId())
                                .type(trend.getResultType())
                                .title(trend.getResultTitle())
                                .subtitle(getSubtitleForSuggestion(trend.getResultType(), trend.getResultId()))
                                .isRecent(false)
                                .isPopular(true)
                                .priority(2)
                                .build());
                    }
                }
            } else {
                // Usuario no autenticado: mostrar solo tendencias filtradas
                List<SearchTrending> trending = searchTrendingRepository
                        .findTop10ByTypeOrderByWeightedScoreDesc(mapFilterToTypes(filter));

                for (SearchTrending t : trending) {
                    items.add(UnifiedSearchItemDTO.builder()
                            .id(t.getResultId())
                            .type(t.getResultType())
                            .title(t.getResultTitle())
                            .subtitle(getSubtitleForSuggestion(t.getResultType(), t.getResultId()))
                            .isRecent(false)
                            .isPopular(true)
                            .priority(2)
                            .build());
                }
            }
        } else {
            // Con query: búsqueda en tiempo real filtrada
            Set<String> userRecentIds = userId != null
                    ? searchHistoryService.getUserRecentSearchIdsByType(userId, mapFilterToTypes(filter))
                    : new HashSet<>();

            Set<String> popularIds = searchHistoryService.getPopularSearchIdsByType(mapFilterToTypes(filter));

            // Aplicar búsquedas según el filtro
            if (filter.equals("all") || filter.equals("movie")) {
                List<MovieSearchResultDTO> movies = titleLogicService.searchMovies(query);
                for (MovieSearchResultDTO movie : movies) {
                    boolean isRecent = userId != null && userRecentIds.contains(movie.getTconst());
                    boolean isPopular = popularIds.contains(movie.getTconst());

                    items.add(UnifiedSearchItemDTO.builder()
                            .id(movie.getTconst())
                            .type("movie")
                            .title(movie.getPrimaryTitle())
                            .subtitle(movie.getStartYear() + (movie.getActors() != null ? " — " + movie.getActors() : ""))
                            .isRecent(isRecent)
                            .isPopular(isPopular && !isRecent)
                            .priority(isRecent ? 1 : (isPopular ? 2 : 3))
                            .build());
                }
            }

            if (filter.equals("all") || filter.equals("serie")) {
                List<SerieSearchResultDTO> series = titleLogicService.searchSeries(query);
                for (SerieSearchResultDTO serie : series) {
                    boolean isRecent = userId != null && userRecentIds.contains(serie.getTconst());
                    boolean isPopular = popularIds.contains(serie.getTconst());

                    String subtitle = serie.getStartYear() +
                            (serie.getEndYear() != null ? " - " + serie.getEndYear() : "") +
                            (serie.getActors() != null ? " — " + serie.getActors() : "");

                    items.add(UnifiedSearchItemDTO.builder()
                            .id(serie.getTconst())
                            .type("serie")
                            .title(serie.getPrimaryTitle())
                            .subtitle(subtitle)
                            .isRecent(isRecent)
                            .isPopular(isPopular && !isRecent)
                            .priority(isRecent ? 1 : (isPopular ? 2 : 3))
                            .build());
                }
            }

            if (filter.equals("all") || filter.equals("person")) {
                List<NameSearchResultDTO> names = nameLogicService.searchNames(query);
                for (NameSearchResultDTO name : names) {
                    boolean isRecent = userId != null && userRecentIds.contains(name.getNconst());
                    boolean isPopular = popularIds.contains(name.getNconst());

                    items.add(UnifiedSearchItemDTO.builder()
                            .id(name.getNconst())
                            .type("person")
                            .title(name.getPrimaryName())
                            .subtitle(buildPersonSubtitle(name))
                            .isRecent(isRecent)
                            .isPopular(isPopular && !isRecent)
                            .priority(isRecent ? 1 : (isPopular ? 2 : 3))
                            .build());
                }
            }

            // Ordenar y limitar a 10
            items = items.stream()
                    .sorted(Comparator.comparingInt(UnifiedSearchItemDTO::getPriority))
                    .limit(10)
                    .collect(Collectors.toList());
        }

        return UnifiedSearchResultDTO.builder()
                .items(items)
                .hasQuery(hasQuery)
                .build();
    }

    // Método auxiliar para mapear filtros a tipos de resultados
    private List<String> mapFilterToTypes(String filter) {
        switch (filter) {
            case "movie":
                return List.of("movie");
            case "serie":
                return List.of("tvSeries","tvMiniSeries");
            case "person":
                return List.of("person");
            case "all":
            default:
                return List.of("movie", "serie", "person");
        }
    }

    // Método auxiliar mejorado para obtener subtítulos
    private String getSubtitleForSuggestion(String type, String id) {
        try {
            if ("movie".equals(type)) {
                Optional<Title> title = titleRepository.findById(id);
                if (title.isPresent()) {
                    Title t = title.get();
                    List<String> actors = getActorNamesForTitle(t.getTconst());
                    return t.getStartYear() + 
                        (!actors.isEmpty() ? " — " + String.join(", ", actors.subList(0, Math.min(3, actors.size()))) : "");
                }
            } else if ("serie".equals(type)) {
                Optional<Title> title = titleRepository.findById(id);
                if (title.isPresent()) {
                    Title t = title.get();
                    List<String> actors = getActorNamesForTitle(t.getTconst());
                    return t.getStartYear() + 
                        (t.getEndYear() != null ? " - " + t.getEndYear() : "") +
                        (!actors.isEmpty() ? " — " + String.join(", ", actors.subList(0, Math.min(3, actors.size()))) : "");
                }
            } else if ("person".equals(type)) {
                Optional<Name> name = nameRepository.findById(id);
                if (name.isPresent()) {
                    Name n = name.get();
                    return n.getPrimaryProfession() != null && !n.getPrimaryProfession().isEmpty() 
                        ? String.join(", ", n.getPrimaryProfession().subList(0, Math.min(3, n.getPrimaryProfession().size())))
                        : "";
                }
            }
        } catch (Exception e) {
            
        }
        return "";
    }

    // Método auxiliar para obtener nombres de actores
    private List<String> getActorNamesForTitle(String tconst) {
        try {
            // 1. Obtener los principals
            List<Principal> principals = principalRepository.findByTitleTconstAndCategoryIn(
                tconst, 
                Arrays.asList("actor", "actress")
            );

            // 2. Extraer los nconst y buscar los nombres
            List<String> actorNconsts = principals.stream()
                .limit(3)
                .map(Principal::getNconst)
                .collect(Collectors.toList());

            // 3. Buscar los nombres en la tabla name_basics
            List<Name> names = nameRepository.findAllById(actorNconsts);

            // 4. Mapear a nombres primarios
            return names.stream()
                .map(Name::getPrimaryName)
                .collect(Collectors.toList());

        } catch (Exception e) {
            return Collections.emptyList();
        }
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
