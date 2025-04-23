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

    public Optional<Episode> getEpisodeById(String tconst) {
        return episodeRepository.findById(tconst);
    }

    public List<Episode> getEpisodesByParentTconst(String parentTconst) {
        return episodeRepository.findByParentTconst(parentTconst);
    }

    public List<Episode> getEpisodesBySeriesAndSeason(String parentTconst, Short seasonNumber) {
        return episodeRepository.findByParentTconstAndSeasonNumber(parentTconst, seasonNumber);
    }
}