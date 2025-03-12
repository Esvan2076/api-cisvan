package com.cisvan.api.component.episode;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.helper.ControllerHelper;

@RestController
@RequestMapping("/title-episodes")
public class TitleEpisodeController {

    @Autowired
    private TitleEpisodeService titleEpisodeService;

    @Autowired
    private ControllerHelper controllerHelper;

    // Buscar un episodio por su ID (tconst)
    @GetMapping("/{id}")
    public ResponseEntity<?> getEpisodeById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(titleEpisodeService.findById(tconst));
    }

    // Buscar episodios de una serie específica (por parent_tconst)
    @GetMapping("/by-series/{parentTconst}")
    public ResponseEntity<List<TitleEpisode>> getEpisodesBySeries(@PathVariable("parentTconst") String parentTconst) {
        List<TitleEpisode> episodes = titleEpisodeService.findByParentTconst(parentTconst);
        if (episodes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(episodes);
    }

    // Buscar episodios de una serie en una temporada específica
    @GetMapping("/by-series/{parentTconst}/season/{seasonNumber}")
    public ResponseEntity<List<TitleEpisode>> getEpisodesBySeason(
            @PathVariable("parentTconst") String parentTconst,
            @PathVariable("seasonNumber") Short seasonNumber) {
        List<TitleEpisode> episodes = titleEpisodeService.findBySeason(parentTconst, seasonNumber);
        if (episodes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(episodes);
    }
}
