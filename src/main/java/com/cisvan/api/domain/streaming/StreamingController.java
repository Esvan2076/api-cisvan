package com.cisvan.api.domain.streaming;

import com.cisvan.api.domain.streaming.services.StreamingService;
import com.cisvan.api.helper.ControllerHelper;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/streaming")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;
    private final ControllerHelper controllerHelper;

    @GetMapping
    public ResponseEntity<List<Streaming>> fetchStreamings() {
        return ResponseEntity.ok(streamingService.getStreamings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchStreamingById(@PathVariable("id") Integer id) {
        return controllerHelper.handleOptional(streamingService.getStreamingById(id));
    }
}
