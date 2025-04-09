package com.cisvan.api.domain.name.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.NameRepository;
import com.cisvan.api.domain.name.dto.NameBasicDTO;
import com.cisvan.api.domain.name.dto.NameSearchResultDTO;
import com.cisvan.api.domain.name.mapper.NameMapper;
import com.cisvan.api.domain.title.TitleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NameLogicService {

    private final NameRepository nameRepository;
    private final TitleRepository titleRepository;
    private final NameMapper nameMapper;

    public List<NameBasicDTO> getNameBasicsDTOsByIds(List<String> nconsts) {
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
}
