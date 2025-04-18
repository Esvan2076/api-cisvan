package com.cisvan.api.domain.title.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.searchDTO.TitleAdvancedSearchDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleService {

    private final TitleRepository titleRepository;

    public Optional<Title> getTitleById(String tconst) {
        return titleRepository.findById(tconst);
    }

    public List<Title> getTitleByName(String name) {
        return titleRepository.findByPrimaryTitleContainingIgnoreCase(name);
    }

    public Page<Title> advancedSearch(TitleAdvancedSearchDTO criteria, Pageable pageable) {
        return titleRepository.advancedSearch(criteria, pageable);
    }    
}
