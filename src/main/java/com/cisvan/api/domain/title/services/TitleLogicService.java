package com.cisvan.api.domain.title.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.episode.Episode;
import com.cisvan.api.domain.episode.EpisodeRepository;
import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.repos.NameRepository;
import com.cisvan.api.domain.principal.repos.PrincipalRepository;
import com.cisvan.api.domain.recommendation.userfinalrecommendation.UserFinalRecommendation;
import com.cisvan.api.domain.recommendation.userfinalrecommendation.UserFinalRecommendationRepository;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.TitleShowDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.MovieSearchResultDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.SerieSearchResultDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.users.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleLogicService {

    private final TitleRepository titleRepository;
    private final NameRepository nameRepository;
    private final PrincipalRepository principalRepository;
    private final EpisodeRepository episodeRepository;
    private final UserFinalRecommendationRepository userFinalRecommendationRepository;
    private final TitleService titleService;

    public void adjustForEpisode(Title title, String tconst) {
        if (!"tvEpisode".equalsIgnoreCase(title.getTitleType())) {
            return;
        }

        Optional<Episode> episodeOpt = episodeRepository.findById(tconst);
        if (episodeOpt.isEmpty()) {
            return;
        }

        Episode episode = episodeOpt.get();

        Optional<String> parentTconstOpt = episodeRepository.findParentTconstByEpisodeTconst(tconst);
        if (parentTconstOpt.isEmpty()) {
            return;
        }

        String parentTconst = parentTconstOpt.get();

        adjustPrimaryTitleWithSeasonPrefix(title, episode);
        replaceOriginalTitleWithSeriesTitle(title, parentTconst);
        replacePosterWithSeriesPoster(title, parentTconst);
    }

    private void adjustPrimaryTitleWithSeasonPrefix(Title title, Episode episode) {
        String seasonEpisodePrefix = episode.getSeasonNumber() + "." + episode.getEpisodeNumber();
        title.setPrimaryTitle(seasonEpisodePrefix + ": " + title.getPrimaryTitle());
    }

    private void replaceOriginalTitleWithSeriesTitle(Title title, String parentTconst) {
        titleRepository.findById(parentTconst).ifPresent(parent -> {
            title.setOriginalTitle(parent.getPrimaryTitle());
        });
    }

    private void replacePosterWithSeriesPoster(Title title, String parentTconst) {
        titleRepository.findById(parentTconst).ifPresent(parent -> {
            title.setPosterUrl(parent.getPosterUrl());
        });
    }

    public List<MovieSearchResultDTO> searchMovies(String query) {
        List<Title> titles = titleRepository.findTop5ByTitleWithRatingOrder(query, PageRequest.of(0, 5));
    
        return titles.stream().map(title -> {
            MovieSearchResultDTO dto = new MovieSearchResultDTO();
            dto.setTconst(title.getTconst());
            dto.setPrimaryTitle(title.getPrimaryTitle());
            dto.setStartYear(title.getStartYear());
            dto.setActors(String.join(", ", getActorNamesForTitle(title.getTconst())));
            return dto;
        }).toList();
    }
    
    public List<SerieSearchResultDTO> searchSeries(String query) {
        List<Title> titles = titleRepository.findTop5SeriesWithRatingOrder(query, PageRequest.of(0, 5));
    
        return titles.stream().map(title -> {
            SerieSearchResultDTO dto = new SerieSearchResultDTO();
            dto.setTconst(title.getTconst());
            dto.setPrimaryTitle(title.getPrimaryTitle());
            dto.setStartYear(title.getStartYear());
            dto.setEndYear(title.getEndYear());
            dto.setActors(String.join(", ", getActorNamesForTitle(title.getTconst())));
            return dto;
        }).toList();
    }    

    private List<String> getActorNamesForTitle(String tconst) {
        List<String> actorIds = principalRepository.findFirst2ActorsByTconst(tconst, PageRequest.of(0, 2));
        return nameRepository.findAllById(actorIds).stream()
            .map(Name::getPrimaryName)
            .toList();
    }

    public List<TitleShowDTO> getTitlesForUser(Users user) {
        List<Object[]> rawResults = titleRepository.findTitlesInUserListWithResolvedPosters(user.getId());

        return rawResults.stream().map(row -> {
            String tconst = (String) row[4]; // original tconst (para saber cuál marcó el usuario)
            String primaryTitle = (String) row[1];
            String posterUrl = (String) row[2];
            BigDecimal rating = (BigDecimal) row[3];

            TitleShowDTO dto = new TitleShowDTO(tconst, primaryTitle, posterUrl, rating);
            dto.setInUserList(true); // Siempre en lista
            return dto;
        }).toList();
    }

    public List<TitleShowDTO> getRecommendedTitlesForUser(Users user) {
        List<UserFinalRecommendation> recommendations =
            userFinalRecommendationRepository.findByUserIdOrderByRankForUserAsc(user.getId());

        return recommendations.stream()
            .map(rec -> titleService.getTitleShowDTOById(rec.getTconst()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .peek(dto -> dto.setInUserList(false)) // Puedes personalizar esto si quieres marcar como ya visto
            .toList();
    }

    public List<TitleShowDTO> getRecommendedTitlesForUserId(Long userId) {
        List<UserFinalRecommendation> recommendations =
            userFinalRecommendationRepository.findByUserIdOrderByRankForUserAsc(userId);

        return recommendations.stream()
            .map(rec -> titleService.getTitleShowDTOById(rec.getTconst()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .peek(dto -> dto.setInUserList(false)) // Opcional: puedes marcar si está en lista
            .toList();
    }
}
