package com.cisvan.api.domain.title;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.domain.title.dtos.searchDTO.MovieSearchResultDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.SerieSearchResultDTO;
import com.cisvan.api.domain.title.services.TitleLogicService;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.helper.ControllerHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/title")
@RequiredArgsConstructor
public class TitleController {

    private final TitleService titleService;
    private final TitleLogicService titleLogicService;
    private final TitleOrchestrator titleOrchestrator;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchTitleById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(titleService.getTitleById(tconst));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<List<Title>> fetchTitleByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(titleService.getTitleByName(name));
    }

    @GetMapping("/basic/{id}")
    public ResponseEntity<?> fetchTitleBasicById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(titleOrchestrator.getTitleBasicById(tconst));
    }

    @GetMapping("/search-movie")
    public ResponseEntity<List<MovieSearchResultDTO>> fetchSearchMovies(@RequestParam("query") String query) {
        return ResponseEntity.ok(titleLogicService.searchMovies(query));
    }

    @GetMapping("/search-serie")
    public ResponseEntity<List<SerieSearchResultDTO>> fetchSearchSeries(@RequestParam("query") String query) {
        return ResponseEntity.ok(titleLogicService.searchSeries(query));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Object>> searchAll(@RequestParam("query") String query) {
        List<Object> results = titleOrchestrator.searchEverything(query);
        return ResponseEntity.ok(results);
    }
}
