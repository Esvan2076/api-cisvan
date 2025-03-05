package com.cisvan.api.component.ratings;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TitleRatingsService {

    @Autowired
    private TitleRatingsRepository titleRatingsRepository;

    public Optional<TitleRatings> findById(String tconst) {
        return titleRatingsRepository.findById(tconst);
    }
}