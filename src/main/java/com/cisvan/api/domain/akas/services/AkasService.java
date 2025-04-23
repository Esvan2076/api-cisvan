package com.cisvan.api.domain.akas.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.akas.Akas;
import com.cisvan.api.domain.akas.AkasRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AkasService {

    private final AkasRepository akasRepository;

    @Transactional(readOnly = true)
    public List<Akas> getAkasById(String tconst) {
        return akasRepository.findByTconst(tconst);
    }
}
