package com.cisvan.api.component.episode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TitleEpisodeService {

    @Autowired
    private TitleEpisodeRepository titleEpisodeRepository;

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
}
