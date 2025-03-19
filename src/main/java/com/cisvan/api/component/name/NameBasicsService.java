package com.cisvan.api.component.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisvan.api.component.name.dto.NameBasicsDTO;
import com.cisvan.api.component.name.mapper.NameBasicsMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NameBasicsService {

    @Autowired
    private NameBasicsRepository nameBasicsRepository;

    @Autowired
    private NameBasicsMapper nameBasicsMapper;

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
}