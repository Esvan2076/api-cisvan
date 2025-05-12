package com.cisvan.api.domain.name;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.domain.name.dto.NameAdvancedSearchDTO;
import com.cisvan.api.domain.name.dto.NameAdvancedSearchResultDTO;
import com.cisvan.api.domain.name.dto.NameSearchResultDTO;
import com.cisvan.api.domain.name.services.NameLogicService;
import com.cisvan.api.domain.name.services.NameService;
import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import com.cisvan.api.helper.ControllerHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/name")
@RequiredArgsConstructor
public class NameController {

    private final NameService nameService;
    private final NameLogicService nameLogicService;
    private final NameOrchestrator nameOrchestrator;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchNameById(@PathVariable("id") String nconst) {
        return controllerHelper.handleOptional(nameService.findById(nconst));
    }

    @GetMapping("/basic/{id}")
    public ResponseEntity<?> fetchTitleBasicById(@PathVariable("id") String tconst) {
        return controllerHelper.handleOptional(nameOrchestrator.getNameBasicById(tconst));
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<List<Name>> fetchNameByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(nameService.findByName(name));
    }

    @GetMapping("/{nconst}/known-for")
    public ResponseEntity<?> fetchNameKnownFor(@PathVariable String nconst) {
        return ResponseEntity.ok(nameLogicService.getKnownForTitles(nconst));
    }

    @GetMapping("/search")
    public ResponseEntity<List<NameSearchResultDTO>> searchNames(@RequestParam("query") String query) {
        return ResponseEntity.ok(nameLogicService.searchNames(query));
    }

    @PostMapping("/advanced-search")
    public ResponseEntity<Page<NameAdvancedSearchResultDTO>> fetchAdvancedNames(
            @RequestBody NameAdvancedSearchDTO filters,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(nameOrchestrator.searchAdvancedNames(filters, page));
    }


    @GetMapping("/{nconst}/works")
    public Page<TitleKnownForDTO> getWorksByPerson(
            @PathVariable String nconst,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return nameService.getWorksByPerson(nconst, pageable);
    }
}
