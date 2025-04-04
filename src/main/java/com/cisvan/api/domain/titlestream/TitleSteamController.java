package com.cisvan.api.domain.titlestream;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.domain.titlestream.services.TitleStreamService;
import com.cisvan.api.helper.RelationResponseHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/title-stream")
@RequiredArgsConstructor
public class TitleSteamController {

    private final TitleStreamService titleStreamService;

    @GetMapping("/streamings/{tconst}")
    public ResponseEntity<RelationResponseHelper> fetchStreamings(@PathVariable String tconst) {
        List<Integer> streamingIds = titleStreamService.getStreamingsByTitleId(tconst);
        return ResponseEntity.ok(new RelationResponseHelper("streamings", streamingIds));
    }
}
