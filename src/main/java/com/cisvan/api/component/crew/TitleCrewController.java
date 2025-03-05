package com.cisvan.api.component.crew;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.helper.ControllerHelper;

@RestController
@RequestMapping("/title-crew")
public class TitleCrewController {
    @Autowired
    private TitleCrewService titleCrewService;

    @Autowired
    private ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCrewById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(titleCrewService.findById(tconst));
    }
}
