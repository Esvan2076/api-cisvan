package com.cisvan.api.component.crew;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TitleCrewService {
    @Autowired
    private TitleCrewRepository titleCrewRepository;
    
    public Optional<TitleCrew> findById(String tconst) {
        return titleCrewRepository.findById(tconst);
    }
}
