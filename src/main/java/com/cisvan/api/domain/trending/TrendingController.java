package com.cisvan.api.domain.trending;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.helper.ControllerHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/trending")
@RequiredArgsConstructor
public class TrendingController {

    private final TrendingService trendingService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchTrendingById(@PathVariable Long id) {
        return controllerHelper.handleOptional(trendingService.getTrendingById(id));
    }

    @GetMapping("/content/{contentId}")
    public ResponseEntity<?> fetchTrendingByTitleId(@PathVariable("contentId") String tconst) {
        return ResponseEntity.ok(trendingService.getTrendingsByTitleId(tconst));
    }
}