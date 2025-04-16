package com.cisvan.api.domain.titlerating.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.titlerating.TitleRating;
import com.cisvan.api.domain.titlerating.TitleRatingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleRatingService {

    private final TitleRatingRepository titleRatingRepository;

    public Optional<TitleRating> getTitleRatingById(String tconst) {
        return titleRatingRepository.findById(tconst);
    }
}