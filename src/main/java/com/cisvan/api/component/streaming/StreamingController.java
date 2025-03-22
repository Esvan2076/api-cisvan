package com.cisvan.api.component.streaming;

import com.cisvan.api.helper.ControllerHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/streaming")
public class StreamingController {
    @Autowired
    private StreamingService streamingService;

    @Autowired
    private ControllerHelper controllerHelper;

    @GetMapping
    public ResponseEntity<List<Streaming>> getAll() {
        return ResponseEntity.ok(streamingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Integer id) {
        return controllerHelper.handleOptional(streamingService.findById(id));
    }
}
