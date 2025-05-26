package com.cisvan.api.domain.title.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import com.cisvan.api.domain.title.dtos.TitleShowDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.TitleAdvancedSearchDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.titlerating.TitleRating;
import com.cisvan.api.domain.titlerating.TitleRatingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleService {

    private final TitleRepository titleRepository;
    private final TitleRatingRepository titleRatingRepository;

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

    public Page<TitleKnownForDTO> advancedSearch(TitleAdvancedSearchDTO criteria, Pageable pageable) {
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

    public Optional<TitleShowDTO> getTitleShowDTOById(String tconst) {
        Optional<Title> titleOpt = titleRepository.findById(tconst);

        if (titleOpt.isEmpty()) {
            return Optional.empty();
        }

        Title title = titleOpt.get();

        // Valor inicial
        BigDecimal averageRating = null;

        // Buscar el rating
        Optional<TitleRating> ratingOpt = titleRatingRepository.findById(tconst);
        if (ratingOpt.isPresent()) {
            averageRating = ratingOpt.get().getAverageRating();
        }

        // Construcción del DTO
        TitleShowDTO dto = TitleShowDTO.builder()
            .tconst(title.getTconst())
            .primaryTitle(title.getPrimaryTitle())
            .posterUrl(title.getPosterUrl()) // getPosterUrl() en DTO ya formatea correctamente
            .averageRating(averageRating)
            .inUserList(false) // Por defecto en este contexto
            .build();

        return Optional.of(dto);
    }
}

