package com.cisvan.api.domain.recommendation;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.domain.title.dtos.RecommendedTitleDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;

    @GetMapping("/{tconst}")
    public List<RecommendedTitleDTO> getRecommendations(@PathVariable String tconst) {
        return recommendationService.getGenreBasedRecommendations(tconst);
    }
}
