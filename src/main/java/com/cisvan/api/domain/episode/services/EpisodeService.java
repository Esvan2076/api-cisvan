package com.cisvan.api.domain.episode.services;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.episode.Episode;
import com.cisvan.api.domain.episode.EpisodeRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EpisodeService {

    private final EpisodeRepository episodeRepository;

    // Buscar un episodio por su ID (tconst)
    public Optional<Episode> getEpisodeById(String tconst) {
        return episodeRepository.findById(tconst);
    }

    // Buscar todos los episodios de una serie (por parent_tconst)
    public List<Episode> getEpisodesByParentTconst(String parentTconst) {
        return episodeRepository.findByParentTconst(parentTconst);
    }

    // Buscar episodios de una serie en una temporada específica
    public List<Episode> getEpisodesBySeriesAndSeason(String parentTconst, Short seasonNumber) {
        return episodeRepository.findByParentTconstAndSeasonNumber(parentTconst, seasonNumber);
    }
}
