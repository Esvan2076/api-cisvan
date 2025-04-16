package com.cisvan.api.domain.namerating.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.namerating.NameRating;
import com.cisvan.api.domain.namerating.NameRatingRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NameRatingService {

    private final NameRatingRepository nameRatingRepository;

    public Optional<NameRating> getNameRatingById(String nconst) {
        return nameRatingRepository.findById(nconst);
    }
}
