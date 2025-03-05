package com.cisvan.api.component.title;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TitleBasicsService {
    @Autowired
    private TitleBasicsRepository titleBasicsRepository;

    public Optional<TitleBasics> findById(String nconst) {
        return titleBasicsRepository.findById(nconst);
    }

    public List<TitleBasics> findByName(String name) {
        return titleBasicsRepository.findByPrimaryTitleContainingIgnoreCase(name);
    }
}
