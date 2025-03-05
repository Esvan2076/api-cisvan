package com.cisvan.api.component.title;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.helper.ControllerHelper;

@RestController
@RequestMapping("/title-basics")
public class TitleBasicsController {
    @Autowired
    private TitleBasicsService titleBasicsService;

    @Autowired
    private ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> getTitleById(@PathVariable("id") String nconst) {
        return controllerHelper.handleOptional(titleBasicsService.findById(nconst));
    }

    // Nuevo método para buscar por nombre
    @GetMapping
    public ResponseEntity<List<TitleBasics>> getByName(@RequestParam("name") String name) {
        List<TitleBasics> results = titleBasicsService.findByName(name);
        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results);
    }
}
