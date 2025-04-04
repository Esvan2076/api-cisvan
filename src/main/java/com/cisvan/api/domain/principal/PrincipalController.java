package com.cisvan.api.domain.principal;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.domain.principal.dtos.CastMemberDTO;
import com.cisvan.api.domain.principal.services.PrincipalService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/principal")
@RequiredArgsConstructor
public class PrincipalController {

    private final PrincipalService principalService;
    
    @GetMapping("/{id}")
    public ResponseEntity<?> fetchPrincipalById(@PathVariable("id") String tconst) {
        return ResponseEntity.ok(principalService.getPrincipalByTitleId(tconst));
    }

    @GetMapping("/{tconst}/cast")
    public ResponseEntity<List<CastMemberDTO>> fetchCastByTconst(@PathVariable String tconst) {
        List<CastMemberDTO> cast = principalService.getCastByTconst(tconst);
        return ResponseEntity.ok(cast);
    }
}
