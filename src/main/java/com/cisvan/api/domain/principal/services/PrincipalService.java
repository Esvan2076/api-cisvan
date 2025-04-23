package com.cisvan.api.domain.principal.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.name.repos.NameRepository;
import com.cisvan.api.domain.principal.Principal;
import com.cisvan.api.domain.principal.PrincipalRepository;
import com.cisvan.api.domain.principal.dtos.CastMemberDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PrincipalService {

    private final PrincipalRepository principalRepository;
    private final NameRepository nameRepository;

    public List<Principal> getPrincipalByTitleId(String tconst) {
        return principalRepository.findByTitleId(tconst);
    }

    @Transactional(readOnly = true)
    public List<CastMemberDTO> getCastByTconst(String tconst) {
        List<Principal> principals = principalRepository.findByIdTconst(tconst);

        return principals.stream()
            .filter(p -> {
                String category = p.getCategory();
                return "actor".equalsIgnoreCase(category) || "actress".equalsIgnoreCase(category);
            })
            .sorted(Comparator.comparing(p -> p.getId().getOrdering()))
            .map(this::mapToCastMemberDTO)
            .toList();
    }

    private CastMemberDTO mapToCastMemberDTO(Principal p) {
        CastMemberDTO dto = new CastMemberDTO();
        dto.setNconst(p.getNconst());

        nameRepository.findById(p.getNconst()).ifPresent(n -> {
            dto.setPrimaryName(n.getPrimaryName());
            dto.setImageUrl(n.getImageUrl());
        });

        dto.setCharacters(p.getCharacters());
        return dto;
    }
}