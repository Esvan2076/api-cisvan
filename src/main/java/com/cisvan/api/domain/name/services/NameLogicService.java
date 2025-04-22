package com.cisvan.api.domain.name.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.dto.NameEssencialDTO;
import com.cisvan.api.domain.name.dto.NameSearchResultDTO;
import com.cisvan.api.domain.name.mapper.NameMapper;
import com.cisvan.api.domain.name.repos.NameRepository;
import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.titlerating.TitleRatingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NameLogicService {

    private final NameRepository nameRepository;
    private final TitleRepository titleRepository;
    private final TitleRatingRepository ratingRepository;
    private final NameMapper nameMapper;

    public List<NameEssencialDTO> getNameBasicsDTOsByIds(List<String> nconsts) {
        return nameRepository.findByNconstIn(nconsts)
                .stream()
                .map(nameMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<NameSearchResultDTO> searchNames(String query) {
        List<Name> people = nameRepository.findTop5ByPrimaryNameIgnoreCaseContainingOrderByPrimaryNameAsc(query);

        return people.stream().map(name -> {
            NameSearchResultDTO dto = nameMapper.toSearchResultDTO(name);

            // Profesión principal
            if (name.getPrimaryProfession() != null && !name.getPrimaryProfession().isEmpty()) {
                dto.setPrimaryProfession(name.getPrimaryProfession().get(0));
            }

            // Obra más conocida (primer título)
            if (name.getKnownForTitles() != null && !name.getKnownForTitles().isEmpty()) {
                String firstTconst = name.getKnownForTitles().get(0);
                titleRepository.findById(firstTconst).ifPresent(title -> {
                    dto.setPrincipalTitle(nameMapper.toPrincipalTitleDTO(title));
                });
            }

            return dto;
        }).toList();
    }

    public List<NameEssencialDTO> findNameBasicsByIds(List<String> nconsts) {
        return nameRepository.findByNconstIn(nconsts)
                .stream()
                .map(nameMapper::toDTO) // Usa MapStruct
                .collect(Collectors.toList());
    }

    public List<TitleKnownForDTO> getKnownForTitles(String nconst) {
        Optional<Name> nameOpt = nameRepository.findById(nconst);
    
        if (nameOpt.isEmpty()) return List.of();
    
        List<String> knownForTitles = nameOpt.get().getKnownForTitles();
        if (knownForTitles == null || knownForTitles.isEmpty()) return List.of();
    
        return knownForTitles.stream()
            .map(tconst -> {
                TitleKnownForDTO dto = new TitleKnownForDTO();
                dto.setTconst(tconst);
    
                titleRepository.findById(tconst).ifPresent(tb -> {
                    dto.setTitleType(tb.getTitleType());
                    dto.setPrimaryTitle(tb.getPrimaryTitle());
                    dto.setStartYear(tb.getStartYear());
                    dto.setPosterUrl(tb.getPosterUrl());
                });
    
                ratingRepository.findById(tconst).ifPresent(dto::setTitleRatings);
    
                return dto;
            })
            .toList();
    }    
}
