package com.cisvan.api.domain.crew.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.crew.Crew;
import com.cisvan.api.domain.crew.CrewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CrewService {

    private final CrewRepository crewRepository;
    
    public Optional<Crew> getCrewById(String tconst) {
        return crewRepository.findById(tconst);
    }

    public Optional<Crew> getCrewByTconst(String tconst) {
        return crewRepository.findById(tconst);
    }    
}
