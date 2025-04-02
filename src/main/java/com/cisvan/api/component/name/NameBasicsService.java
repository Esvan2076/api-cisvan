package com.cisvan.api.component.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisvan.api.component.name.dto.NameBasicsDTO;
import com.cisvan.api.component.name.dto.TitleKnownForDTO;
import com.cisvan.api.component.name.mapper.NameBasicsMapper;
import com.cisvan.api.component.ratings.TitleRatingsRepository;
import com.cisvan.api.component.title.TitleBasicsRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NameBasicsService {

    @Autowired
    private NameBasicsRepository nameBasicsRepository;

    @Autowired
    private NameBasicsMapper nameBasicsMapper;

    @Autowired
    private TitleBasicsRepository titleBasicsRepository;

    @Autowired
    private TitleRatingsRepository titleRatingsRepository;

    public Optional<NameBasics> findById(String nconst) {
        return nameBasicsRepository.findById(nconst);
    }

    public List<NameBasics> findByName(String name) {
        return nameBasicsRepository.findByPrimaryNameContainingIgnoreCase(name);
    }

    public List<NameBasicsDTO> findNameBasicsByIds(List<String> nconsts) {
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