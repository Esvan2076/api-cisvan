package com.cisvan.api.domain.name;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.domain.name.dto.NameSearchResultDTO;
import com.cisvan.api.domain.name.services.NameLogicService;
import com.cisvan.api.domain.name.services.NameService;
import com.cisvan.api.helper.ControllerHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/name")
@RequiredArgsConstructor
public class NameController {

    private final NameService nameService;
    private final NameLogicService nameLogicService;
    private final ControllerHelper controllerHelper;

    @GetMapping("/{id}")
    public ResponseEntity<?> fetchNameById(@PathVariable("id") String nconst) {
        return controllerHelper.handleOptional(nameService.findById(nconst));
    }    

    @GetMapping("/by-name/{name}")
    public ResponseEntity<List<Name>> fetchNameByName(@PathVariable("name") String name) {
        return ResponseEntity.ok(nameService.findByName(name));
    }

    @GetMapping("/{nconst}/known-for")
    public ResponseEntity<?> fetchNameKnownFor(@PathVariable String nconst) {
        return ResponseEntity.ok(nameService.getKnownForTitles(nconst));
    }

    @GetMapping("/search")
    public ResponseEntity<List<NameSearchResultDTO>> searchNames(@RequestParam("query") String query) {
        return ResponseEntity.ok(nameLogicService.searchNames(query));
    }
}
