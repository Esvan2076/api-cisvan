package com.cisvan.api.domain.akas;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.domain.akas.services.AkasService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/akas")
@RequiredArgsConstructor
public class AkasController {

    private final AkasService akasService;

    @GetMapping("/{id}")
    public ResponseEntity<List<Akas>> fetchAkasById(@PathVariable("id") String tconst) {
        return ResponseEntity.ok(akasService.getAkasById(tconst));
    }
}
