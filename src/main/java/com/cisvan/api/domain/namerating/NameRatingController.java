package com.cisvan.api.domain.namerating;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.domain.namerating.services.NameRatingService;
import com.cisvan.api.helper.ControllerHelper;

@RestController
@RequestMapping("/name-rating")
@RequiredArgsConstructor
public class NameRatingController {

    private final NameRatingService nameRatingService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchNameRatingById(@PathVariable("id") String nconst) {
        return controllerHelper.handleOptional(nameRatingService.getNameRatingById(nconst));
    }
}
