package com.cisvan.api.domain.name.services;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.NameRepository;
import com.cisvan.api.domain.name.dto.NameBasicDTO;
import com.cisvan.api.domain.name.mapper.NameMapper;
import com.cisvan.api.domain.rating.RatingRepository;
import com.cisvan.api.domain.title.TitleRepository;
import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NameService {

    private final NameRepository nameBasicsRepository;
    private final NameMapper nameBasicsMapper;
    private final TitleRepository titleBasicsRepository;
    private final RatingRepository titleRatingsRepository;

    public Optional<Name> findById(String nconst) {
        return nameBasicsRepository.findById(nconst);
    }

    public List<Name> findByName(String name) {
        return nameBasicsRepository.findByPrimaryNameContainingIgnoreCase(name);
    }

    public List<NameBasicDTO> findNameBasicsByIds(List<String> nconsts) {
        return nameBasicsRepository.findByNconstIn(nconsts)
                .stream()
                .map(nameBasicsMapper::toDTO) // Usa MapStruct
                .collect(Collectors.toList());
    }

    public List<TitleKnownForDTO> getKnownForTitles(String nconst) {
        return nameBasicsRepository.findById(nconst)
            .map(name -> name.getKnownForTitles().stream()
                .map(tconst -> {
                    TitleKnownForDTO dto = new TitleKnownForDTO();
                    dto.setTconst(tconst);
    
                    titleBasicsRepository.findById(tconst).ifPresent(tb -> {
                        dto.setTitleType(tb.getTitleType());
                        dto.setPrimaryTitle(tb.getPrimaryTitle());
                        dto.setStartYear(tb.getStartYear());
                        dto.setPosterUrl(tb.getPosterUrl()); // ya viene con la URL completa
                    });
    
                    titleRatingsRepository.findById(tconst).ifPresent(dto::setRatings);
    
                    return dto;
                }).toList()
            ).orElse(List.of());
    }
}