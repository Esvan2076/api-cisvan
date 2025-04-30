package com.cisvan.api.domain.title.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.TitleShowDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.TitleAdvancedSearchDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleService {

    private final TitleRepository titleRepository;

    public boolean existsById(String tconst) {
        return titleRepository.existsById(tconst);
    }

    public Optional<Title> getTitleById(String tconst) {
        return titleRepository.findById(tconst);
    }

    public List<TitleShowDTO> getTitlesById(List<String> tconst) {
        return titleRepository.findByTconstIn(tconst);
    }

    public List<Title> getTitleByName(String name) {
        return titleRepository.findByPrimaryTitleContainingIgnoreCase(name);
    }

    public Page<Title> advancedSearch(TitleAdvancedSearchDTO criteria, Pageable pageable) {
        return titleRepository.advancedSearch(criteria, pageable);
    }

    public List<TitleShowDTO> getTop20Series() {
        return titleRepository.findTop20Series();
    }

    public List<TitleShowDTO> getTop20NonSeries() {
        return titleRepository.findTop20NonSeries();
    }

    public List<TitleShowDTO> getTop20Trending() {
        // Paso 1: Obtener los 20 contentId más populares de Trending
        List<String> topContentIds = titleRepository.findTop20TrendingContentIds();
    
        // Paso 2: Obtener los detalles de los títulos y calificaciones para esos contentId
        List<TitleShowDTO> trendingContentDetails = titleRepository.findTrendingContentDetails(topContentIds);
    
        return trendingContentDetails;
    }
}

