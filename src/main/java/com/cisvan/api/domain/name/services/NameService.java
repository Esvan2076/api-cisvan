package com.cisvan.api.domain.name.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.dto.NameAdvancedSearchDTO;

import com.cisvan.api.domain.name.repos.NameRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NameService {

    private final NameRepository nameRepository;

    public Optional<Name> findById(String nconst) {
        return nameRepository.findById(nconst);
    }

    public List<Name> findByName(String name) {
        return nameRepository.findByPrimaryNameContainingIgnoreCase(name);
    }

    @Transactional(readOnly = true)
    public Page<Name> advancedSearch(NameAdvancedSearchDTO criteria, Pageable pageable) {
        return nameRepository.advancedSearch(criteria, pageable);
    }
}