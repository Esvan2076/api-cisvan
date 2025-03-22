package com.cisvan.api.component.titlestream;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.helper.RelationResponseHelper;

@RestController
@RequestMapping("/title-stream")
public class TitleSteamController {
    @Autowired
    private TitleStreamService titleStreamService;

    @GetMapping("/streamings/{tconst}")
    public ResponseEntity<RelationResponseHelper> getStreamings(@PathVariable String tconst) {
        List<Integer> streamingIds = titleStreamService.getStreamingsOfTitle(tconst);
        return ResponseEntity.ok(new RelationResponseHelper("streamings", streamingIds));
    }
}
