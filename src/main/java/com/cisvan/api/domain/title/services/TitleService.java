package com.cisvan.api.domain.title.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.TitleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleService {

    private final TitleRepository titleRepository;

    public Optional<Title> getTitleById(String tconst) {
        return titleRepository.findById(tconst);
    }

    public List<Title> getTitleByName(String name) {
        return titleRepository.findByPrimaryTitleContainingIgnoreCase(name);
    }
}
