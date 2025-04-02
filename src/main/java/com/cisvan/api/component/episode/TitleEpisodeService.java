package com.cisvan.api.component.episode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisvan.api.component.ratings.TitleRatings;
import com.cisvan.api.component.ratings.TitleRatingsRepository;
import com.cisvan.api.component.title.TitleBasics;
import com.cisvan.api.component.title.TitleBasicsRepository;
import com.cisvan.api.component.title.dto.EpisodeSummaryDTO;
import com.cisvan.api.component.title.dto.SeriesSeasonSummaryDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TitleEpisodeService {

    @Autowired
    private TitleEpisodeRepository titleEpisodeRepository;

    @Autowired
    private TitleBasicsRepository titleBasicsRepository;

    @Autowired
    private TitleRatingsRepository titleRatingsRepository;

    // Buscar un episodio por su ID (tconst)
    public Optional<TitleEpisode> findById(String tconst) {
        return titleEpisodeRepository.findById(tconst);
    }

    // Buscar todos los episodios de una serie (por parent_tconst)
    public List<TitleEpisode> findByParentTconst(String parentTconst) {
        return titleEpisodeRepository.findByParentTconst(parentTconst);
    }

    // Buscar episodios de una serie en una temporada específica
    public List<TitleEpisode> findBySeason(String parentTconst, Short seasonNumber) {
        return titleEpisodeRepository.findByParentTconstAndSeasonNumber(parentTconst, seasonNumber);
    }

    public Optional<SeriesSeasonSummaryDTO> getSeriesSeasonSummary(String tconst) {
        // Buscar el título original
        Optional<TitleBasics> titleOpt = titleBasicsRepository.findById(tconst);
        if (titleOpt.isEmpty()) {
            return Optional.empty();
        }

        TitleBasics title = titleOpt.get();
        String seriesTconst;
        TitleBasics seriesTitle;

        if ("tvSeries".equalsIgnoreCase(title.getTitleType())) {
            seriesTconst = title.getTconst();
            seriesTitle = title;
        } else if ("tvEpisode".equalsIgnoreCase(title.getTitleType())) {
            Optional<String> parentTconstOpt = titleEpisodeRepository.findParentTconstByEpisodeTconst(tconst);
            if (parentTconstOpt.isEmpty()) {
                return Optional.empty();
            }

            seriesTconst = parentTconstOpt.get();
            Optional<TitleBasics> parentTitleOpt = titleBasicsRepository.findById(seriesTconst);
            if (parentTitleOpt.isEmpty()) {
                return Optional.empty();
            }
            seriesTitle = parentTitleOpt.get();
        } else {
            return Optional.empty(); // No aplica para otros tipos
        }

        // Obtener cantidad de temporadas únicas
        Integer seasonCount = titleEpisodeRepository.countDistinctSeasonsByParentTconst(seriesTconst);

        // Armar DTO
        SeriesSeasonSummaryDTO dto = new SeriesSeasonSummaryDTO();
        dto.setSeriesTconst(seriesTconst);
        dto.setSeriesTitle(seriesTitle.getPrimaryTitle());
        dto.setTotalSeasons(seasonCount);

        return Optional.of(dto);
    }

    public List<EpisodeSummaryDTO> getEpisodesBySeason(String tconst, Short seasonNumber) {
        // Si es un episodio, conseguir la serie padre
        Optional<TitleBasics> titleOpt = titleBasicsRepository.findById(tconst);
        if (titleOpt.isEmpty()) return List.of();

        TitleBasics title = titleOpt.get();
        String parentTconst;

        if ("tvSeries".equalsIgnoreCase(title.getTitleType())) {
            parentTconst = title.getTconst();
        } else if ("tvEpisode".equalsIgnoreCase(title.getTitleType())) {
            Optional<String> parentOpt = titleEpisodeRepository.findParentTconstByEpisodeTconst(tconst);
            if (parentOpt.isEmpty()) return List.of();
            parentTconst = parentOpt.get();
        } else {
            return List.of();
        }

        // Buscar episodios por serie y temporada
        List<TitleEpisode> episodes = titleEpisodeRepository.findByParentTconstAndSeasonNumber(parentTconst, seasonNumber);

        return episodes.stream()
            .map(ep -> {
                Optional<TitleBasics> epTitleOpt = titleBasicsRepository.findById(ep.getTconst());
                if (epTitleOpt.isEmpty()) return null;

                TitleBasics epTitle = epTitleOpt.get();
                Optional<TitleRatings> ratingOpt = titleRatingsRepository.findById(ep.getTconst());

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

}
