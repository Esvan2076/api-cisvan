package com.cisvan.api.domain.titlerating;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.domain.titlerating.services.TitleRatingService;
import com.cisvan.api.helper.ControllerHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/title-rating")
@RequiredArgsConstructor
public class TitleRatingController {

    private final TitleRatingService ratingService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchTitleRatingById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(ratingService.getTitleRatingById(tconst));
    }
}
