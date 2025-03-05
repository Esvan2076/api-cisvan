package com.cisvan.api.component.ratings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.helper.ControllerHelper;

@RestController
@RequestMapping("/title-ratings")
public class TitleRatingsController {
    @Autowired
    private TitleRatingsService titleRatingsService;

    @Autowired
    private ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> getRatingById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(titleRatingsService.findById(tconst));
    }
}
