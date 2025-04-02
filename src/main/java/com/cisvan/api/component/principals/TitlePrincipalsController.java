package com.cisvan.api.component.principals;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.component.principals.dtos.CastMemberDTO;

@RestController
@RequestMapping("/title-principals")
public class TitlePrincipalsController {
    @Autowired
    private TitlePrincipalsService titlePrincipalsService;
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getPrincipalsById(@PathVariable("id") String tconst) {
        return ResponseEntity.ok(titlePrincipalsService.findById(tconst));
    }

    @GetMapping("/{tconst}/cast")
    public ResponseEntity<List<CastMemberDTO>> getCastByTconst(@PathVariable String tconst) {
        List<CastMemberDTO> cast = titlePrincipalsService.getCastByTconst(tconst);
        return ResponseEntity.ok(cast);
    }
}
