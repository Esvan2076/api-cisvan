package com.cisvan.api.domain.crew;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.domain.crew.services.CrewService;
import com.cisvan.api.helper.ControllerHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/crew")
@RequiredArgsConstructor
public class CrewController {

    private final CrewService crewService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchCrewById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(crewService.getCrewById(tconst));
    }
}
