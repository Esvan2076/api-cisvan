package com.cisvan.api.component.name;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.helper.ControllerHelper;

@RestController
@RequestMapping("/name-basics")
public class NameBasicsController {

    @Autowired
    private NameBasicsService nameBasicsService;

    @Autowired
    private ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> getNameById(@PathVariable("id") String nconst) {
        return controllerHelper.handleOptional(nameBasicsService.findById(nconst));
    }

    // Nuevo método para buscar por nombre
    @GetMapping
    public ResponseEntity<List<NameBasics>> getByName(@RequestParam("name") String name) {
        List<NameBasics> results = nameBasicsService.findByName(name);
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results);
    }
}