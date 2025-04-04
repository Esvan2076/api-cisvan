package com.cisvan.api.domain.rating.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.rating.Rating;
import com.cisvan.api.domain.rating.RatingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;

    public Optional<Rating> getRatingById(String tconst) {
        return ratingRepository.findById(tconst);
    }
}