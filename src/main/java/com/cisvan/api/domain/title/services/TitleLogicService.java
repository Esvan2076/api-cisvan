package com.cisvan.api.domain.title.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.episode.Episode;
import com.cisvan.api.domain.episode.EpisodeRepository;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.TitleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleLogicService {

    private final TitleRepository titleRepository;
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
}
