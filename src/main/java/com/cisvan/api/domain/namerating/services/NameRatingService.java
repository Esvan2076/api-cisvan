package com.cisvan.api.domain.namerating.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.namerating.NameRating;
import com.cisvan.api.domain.namerating.NameRatingRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NameRatingService {

    private final NameRatingRepository nameRatingRepository;

    @Transactional(readOnly = true)
    public Optional<NameRating> getNameRatingById(String nconst) {
        return nameRatingRepository.findById(nconst);
    }
}
