package com.cisvan.api.domain.name.services;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.NameRepository;
import com.cisvan.api.domain.name.dto.NameEssencialDTO;
import com.cisvan.api.domain.name.mapper.NameMapper;
import com.cisvan.api.domain.title.TitleRepository;
import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import com.cisvan.api.domain.titlerating.TitleRatingRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NameService {

    private final NameRepository nameRepository;
    private final NameMapper nameMapper;
    private final TitleRepository titleRepository;
    private final TitleRatingRepository ratingRepository;

    public Optional<Name> findById(String nconst) {
        return nameRepository.findById(nconst);
    }

    public List<Name> findByName(String name) {
        return nameRepository.findByPrimaryNameContainingIgnoreCase(name);
    }

    public List<NameEssencialDTO> findNameBasicsByIds(List<String> nconsts) {
        return nameRepository.findByNconstIn(nconsts)
                .stream()
                .map(nameMapper::toDTO) // Usa MapStruct
                .collect(Collectors.toList());
    }

    public List<TitleKnownForDTO> getKnownForTitles(String nconst) {
        return nameRepository.findById(nconst)
            .map(name -> name.getKnownForTitles().stream()
                .map(tconst -> {
                    TitleKnownForDTO dto = new TitleKnownForDTO();
                    dto.setTconst(tconst);
    
                    titleRepository.findById(tconst).ifPresent(tb -> {
                        dto.setTitleType(tb.getTitleType());
                        dto.setPrimaryTitle(tb.getPrimaryTitle());
                        dto.setStartYear(tb.getStartYear());
                        dto.setPosterUrl(tb.getPosterUrl()); // ya viene con la URL completa
                    });
    
                    ratingRepository.findById(tconst).ifPresent(dto::setTitleRatings);
    
                    return dto;
                }).toList()
            ).orElse(List.of());
    }
}