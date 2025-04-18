package com.cisvan.api.domain.episode.services;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.episode.Episode;
import com.cisvan.api.domain.episode.EpisodeRepository;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.EpisodeSummaryDTO;
import com.cisvan.api.domain.title.dtos.SeriesSeasonsDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.titlerating.TitleRating;
import com.cisvan.api.domain.titlerating.TitleRatingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EpisodeLogicService {

    private final EpisodeRepository episodeRepository;
    private final TitleRepository titleRepository;
    private final TitleRatingRepository ratingRepository;

    // Determinar el tconst de búsqueda, usando el parent si es episodio
    public String resolveSearchTconst(String tconst, String titleType) {
        if (!"tvEpisode".equalsIgnoreCase(titleType)) {
            return tconst;
        }
        return episodeRepository.findParentTconstByEpisodeTconst(tconst).orElse(tconst);
    }

    // Obtener el resumen de temporadas de una serie desde un tconst (serie o episodio)
    public Optional<SeriesSeasonsDTO> getSeriesSeasonSummary(String tconst) {
        Optional<Title> seriesOpt = resolveParentSeries(tconst);
        if (seriesOpt.isEmpty()) {
            return Optional.empty();
        }

        Title series = seriesOpt.get();
        Integer seasonCount = episodeRepository.countDistinctSeasonsByParentTconst(series.getTconst());

        SeriesSeasonsDTO dto = new SeriesSeasonsDTO();
        dto.setSeriesTconst(series.getTconst());
        dto.setSeriesTitle(series.getPrimaryTitle());
        dto.setTotalSeasons(seasonCount);

        return Optional.of(dto);
    }

    // Obtener los episodios de una temporada para una serie dada
    public List<EpisodeSummaryDTO> getEpisodesBySeason(String tconst, Short seasonNumber) {
        Optional<Title> seriesOpt = resolveParentSeries(tconst);
        if (seriesOpt.isEmpty()) return List.of();

        String parentTconst = seriesOpt.get().getTconst();

        List<Episode> episodes = episodeRepository.findByParentTconstAndSeasonNumber(parentTconst, seasonNumber);

        return episodes.stream()
            .map(ep -> {
                Optional<Title> epTitleOpt = titleRepository.findById(ep.getTconst());
                if (epTitleOpt.isEmpty()) return null;

                Title epTitle = epTitleOpt.get();
                Optional<TitleRating> ratingOpt = ratingRepository.findById(ep.getTconst());

                EpisodeSummaryDTO dto = new EpisodeSummaryDTO();
                dto.setEpisodeNumber(ep.getEpisodeNumber());
                dto.setTconst(ep.getTconst());
                dto.setPrimaryTitle(epTitle.getPrimaryTitle());
                ratingOpt.ifPresent(r -> {
                    dto.setAverageRating(r.getAverageRating());
                    dto.setNumVotes(r.getNumVotes());
                });
                return dto;
            })
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(EpisodeSummaryDTO::getEpisodeNumber))
            .toList();
    }

    // Resolver la serie base desde un tconst (puede ser serie o episodio)
    private Optional<Title> resolveParentSeries(String tconst) {
        Optional<Title> titleOpt = titleRepository.findById(tconst);
        if (titleOpt.isEmpty()) return Optional.empty();

        Title title = titleOpt.get();

        if ("tvSeries".equalsIgnoreCase(title.getTitleType())) {
            return Optional.of(title);
        }

        if ("tvEpisode".equalsIgnoreCase(title.getTitleType())) {
            return episodeRepository.findParentTconstByEpisodeTconst(tconst)
                    .flatMap(titleRepository::findById);
        }

        return Optional.empty();
    }
}
