package com.cisvan.api.domain.title.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.episode.Episode;
import com.cisvan.api.domain.episode.EpisodeRepository;
import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.NameRepository;
import com.cisvan.api.domain.principal.PrincipalRepository;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.TitleRepository;
import com.cisvan.api.domain.title.dtos.searchDTO.MovieSearchResultDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.SerieSearchResultDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleLogicService {

    private final TitleRepository titleRepository;
    private final NameRepository nameRepository;
    private final PrincipalRepository principalRepository;
    private final EpisodeRepository episodeRepository;

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
}
