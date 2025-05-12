package com.cisvan.api.domain.principal.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;

public interface PrincipalCustomRepository {
    
    Page<TitleKnownForDTO> findWorksByPerson(String nconst, Pageable pageable);
}