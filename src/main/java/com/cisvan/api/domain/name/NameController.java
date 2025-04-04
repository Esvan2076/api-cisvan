package com.cisvan.api.domain.name;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.domain.name.services.NameService;
import com.cisvan.api.helper.ControllerHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/name")
@RequiredArgsConstructor
public class NameController {

    private final NameService nameService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchNameById(@PathVariable("id") String nconst) {
        return controllerHelper.handleOptional(nameService.findById(nconst));
    }

    @GetMapping("/by-name")
    public ResponseEntity<List<Name>> fetchNameByName(@RequestParam("name") String name) {
        return ResponseEntity.ok(nameService.findByName(name));
    }

    @GetMapping("/{nconst}/known-for")
    public ResponseEntity<?> fetchNameKnownFor(@PathVariable String nconst) {
        return ResponseEntity.ok(nameService.getKnownForTitles(nconst));
    }
}
