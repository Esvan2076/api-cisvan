package com.cisvan.api.component.name;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NameBasicsService {

    @Autowired
    private NameBasicsRepository nameBasicsRepository;

    public Optional<NameBasics> findById(String nconst) {
        return nameBasicsRepository.findById(nconst);
    }

    public List<NameBasics> findByName(String name) {
        return nameBasicsRepository.findByPrimaryNameContainingIgnoreCase(name);
    }
}