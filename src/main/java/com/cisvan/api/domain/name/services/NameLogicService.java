package com.cisvan.api.domain.name.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.name.NameRepository;
import com.cisvan.api.domain.name.dto.NameBasicDTO;
import com.cisvan.api.domain.name.mapper.NameMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NameLogicService {

    private final NameRepository nameRepository;
    private final NameMapper nameMapper;

    public List<NameBasicDTO> getNameBasicsDTOsByIds(List<String> nconsts) {
        return nameRepository.findByNconstIn(nconsts)
                .stream()
                .map(nameMapper::toDTO)
                .collect(Collectors.toList());
    }
}
