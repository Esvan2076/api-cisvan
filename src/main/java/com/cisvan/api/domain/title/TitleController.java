package com.cisvan.api.domain.title;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import com.cisvan.api.domain.title.dtos.TitleReviewDTO;
import com.cisvan.api.domain.title.dtos.UnifiedSearchResultDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.TitleAdvancedSearchDTO;
import com.cisvan.api.domain.title.services.TitleReviewService;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;
import com.cisvan.api.helper.ControllerHelper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/title")
@RequiredArgsConstructor
public class TitleController {

    private final TitleService titleService;
    private final TitleOrchestrator titleOrchestrator;
    private final ControllerHelper controllerHelper;
    private final TitleReviewService titleReviewService;
    private final UserLogicService userLogicService;

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchTitleById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(titleService.getTitleById(tconst));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<List<Title>> fetchTitleByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(titleService.getTitleByName(name));
    }

    @GetMapping("/basic/{id}")
    public ResponseEntity<?> fetchTitleBasicById(@PathVariable("id") String tconst, HttpServletRequest request) {
        return controllerHelper.handleOptional(titleOrchestrator.getTitleBasicById(tconst, request));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Object>> searchAll(@RequestParam("query") String query) {
        List<Object> results = titleOrchestrator.searchEverything(query);
        return ResponseEntity.ok(results);
    }

    // Agregar después del método searchAll existente
    @GetMapping("/search/unified")
    public ResponseEntity<UnifiedSearchResultDTO> unifiedSearch(
            @RequestParam("query") String query,
            @RequestParam(value = "filter", defaultValue = "all") String filter,
            HttpServletRequest request) {
        
        // Hacer opcional la autenticación
        Long userId = null;
        try {
            Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
            userId = userOpt.map(Users::getId).orElse(null);
        } catch (Exception e) {
            // Si falla la autenticación, continuar sin usuario
            userId = null;
        }
        
        UnifiedSearchResultDTO results = titleOrchestrator.unifiedSearch(query, userId, filter);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/advanced-search")
    public ResponseEntity<Page<TitleKnownForDTO>> fetchTitleWithAdvancedSearch(
            @RequestBody TitleAdvancedSearchDTO filters,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(titleOrchestrator.searchAdvancedTitles(filters, page));
    }

    @GetMapping("/user-list")
    public ResponseEntity<?> fetchListUser(HttpServletRequest request) {
        return ResponseEntity.ok(titleOrchestrator.getListOfUser(request));
    }

    @GetMapping("/top-series")
    public ResponseEntity<?> fetchTopSeries(HttpServletRequest request) {
        return ResponseEntity.ok(titleOrchestrator.getTop20Series(request));
    }

    @GetMapping("/top-movies")
    public ResponseEntity<?> fetchTopNonSeries(HttpServletRequest request) {
        return ResponseEntity.ok(titleOrchestrator.getTop20NonSeries(request));
    }

    @GetMapping("/top-trending")
    public ResponseEntity<?> fetchTopTrending(HttpServletRequest request) {
        return ResponseEntity.ok(titleOrchestrator.getTop20Trending(request));
    }

    @GetMapping("/{tconst}/review-data")
    public ResponseEntity<?> getTitleReviewData(@PathVariable String tconst) {
        Optional<TitleReviewDTO> reviewData = titleReviewService.getTitleReview(tconst);

        if (reviewData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Title not found");
        }

        return ResponseEntity.ok(reviewData.get());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<?> fetchFinalRecommendations(HttpServletRequest request) {
        return ResponseEntity.ok(titleOrchestrator.getFinalRecommendations(request));
    }

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<?> fetchFinalRecommendationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(titleOrchestrator.getFinalRecommendationsByUserId(userId));
    }
}
