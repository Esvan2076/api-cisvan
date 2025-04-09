package com.cisvan.api.domain.episode;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.domain.episode.services.EpisodeLogicService;
import com.cisvan.api.domain.episode.services.EpisodeService;
import com.cisvan.api.domain.title.dtos.EpisodeSummaryDTO;
import com.cisvan.api.helper.ControllerHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/episode")
@RequiredArgsConstructor
public class EpisodeController {

    private final EpisodeService episodeService;
    private final EpisodeLogicService episodeLogicService;
    private final ControllerHelper controllerHelper;

    // Buscar un episodio por su ID (tconst)
    @GetMapping("/{id}")
    public ResponseEntity<?> fetchEpisodeById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(episodeService.getEpisodeById(tconst));
    }

    // Buscar episodios de una serie específica (por parent_tconst)
    @GetMapping("/by-series/{parentTconst}")
    public ResponseEntity<List<Episode>> fetchEpisodesBySeries(@PathVariable("parentTconst") String parentTconst) {
        return ResponseEntity.ok(episodeService.getEpisodesByParentTconst(parentTconst));
    }

    // Buscar episodios de una serie en una temporada específica
    @GetMapping("/by-series/{parentTconst}/season/{seasonNumber}")
    public ResponseEntity<List<EpisodeSummaryDTO>> getEpisodesBySeason(
            @PathVariable("parentTconst") String parentTconst,
            @PathVariable("seasonNumber") Short seasonNumber) {
        return ResponseEntity.ok(episodeLogicService.getEpisodesBySeason(parentTconst, seasonNumber));
    }

    @GetMapping("/series/summary/{tconst}")
    public ResponseEntity<?> getSeriesSummary(@PathVariable String tconst) {
        return controllerHelper.handleOptional(episodeLogicService.getSeriesSeasonSummary(tconst));
    }

    @GetMapping("/series/{tconst}/season/{seasonNumber}")
    public ResponseEntity<?> getEpisodesForSeason(
        @PathVariable String tconst,
        @PathVariable Short seasonNumber
    ) {
        List<EpisodeSummaryDTO> episodes = episodeLogicService.getEpisodesBySeason(tconst, seasonNumber);
        return ResponseEntity.ok(episodes);
    }
}
