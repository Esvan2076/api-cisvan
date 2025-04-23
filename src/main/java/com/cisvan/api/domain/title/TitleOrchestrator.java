package com.cisvan.api.domain.title;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.akas.services.AkasLogicService;
import com.cisvan.api.domain.crew.CrewRepository;
import com.cisvan.api.domain.name.dto.NameEssencialDTO;
import com.cisvan.api.domain.name.services.NameLogicService;
import com.cisvan.api.domain.streaming.services.StreamingLogicService;
import com.cisvan.api.domain.title.dtos.TitleBasicDTO;
import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import com.cisvan.api.domain.title.dtos.searchDTO.TitleAdvancedSearchDTO;
import com.cisvan.api.domain.title.mappers.TitleMapper;
import com.cisvan.api.domain.title.services.TitleLogicService;
import com.cisvan.api.domain.title.services.TitleService;
import com.cisvan.api.domain.titlerating.TitleRatingRepository;
import com.cisvan.api.domain.titlerating.services.TitleRatingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleOrchestrator {

    private final CrewRepository crewRepository;
    private final TitleRatingRepository ratingRepository;
    private final TitleLogicService titleLogicService;
    private final TitleService titleService;
    private final NameLogicService nameLogicService;
    private final TitleMapper titleMapper;
    private final StreamingLogicService streamingLogicService;
    private final AkasLogicService akasLogicService;
    private final TitleRatingService titleRatingService;

    public Optional<TitleBasicDTO> getTitleBasicById(String tconst) {
        Optional<Title> titleOpt = titleService.getTitleById(tconst);
        if (titleOpt.isEmpty()) {
            return Optional.empty();
        }

        Title title = titleOpt.get();

        // Ajustes si es episodio
        titleLogicService.adjustForEpisode(title, tconst);

        TitleBasicDTO detailDTO = titleMapper.toDTO(title);

        // Directores y escritores
        crewRepository.findById(tconst).ifPresent(titleCrew -> {
            List<String> directors = Optional.ofNullable(titleCrew.getDirectors()).orElse(Collections.emptyList());
            List<String> writers = Optional.ofNullable(titleCrew.getWriters()).orElse(Collections.emptyList());
        
            List<NameEssencialDTO> directos = nameLogicService.getNameBasicsDTOsByIds(
                directors.stream().limit(3).toList()
            );
            List<NameEssencialDTO> writersList = nameLogicService.getNameBasicsDTOsByIds(
                writers.stream().limit(3).toList()
            );
        
            detailDTO.setDirectos(directos);
            detailDTO.setWriters(writersList);
        });        

        Locale locale = LocaleContextHolder.getLocale();
        if ("es".equalsIgnoreCase(locale.getLanguage())) {
            // Cambiar el título si hay una traducción al español
            akasLogicService.trySetSpanishTitle(detailDTO);
        }

        // Ratings
        ratingRepository.findById(tconst).ifPresent(detailDTO::setTitleRatings);

        // Streaming
        detailDTO.setStreamingServices(streamingLogicService.getStreamingServicesByTitle(tconst));

        return Optional.of(detailDTO);
    }

    public List<Object> searchEverything(String query) {
        List<Object> results = new ArrayList<>();

        results.addAll(titleLogicService.searchMovies(query));
        results.addAll(titleLogicService.searchSeries(query));
        results.addAll(nameLogicService.searchNames(query));

        return results.stream().limit(10).toList();
    }

    public Page<TitleKnownForDTO> searchAdvancedTitles(TitleAdvancedSearchDTO criteria, int page) {
        Pageable pageable = PageRequest.of(page, 20); // Tamaño fijo de 20 como se solicitó
        Page<Title> titlePage = titleService.advancedSearch(criteria, pageable);

        List<TitleKnownForDTO> content = titlePage.getContent().stream().map(title -> {
            TitleKnownForDTO dto = titleMapper.toKnownForDTO(title);

            // Obtener el rating de forma separada
            titleRatingService.getTitleRatingById(title.getTconst())
                    .ifPresent(dto::setTitleRatings);

            return dto;
        }).toList();

        return new PageImpl<>(content, pageable, titlePage.getTotalElements());
    }
}
