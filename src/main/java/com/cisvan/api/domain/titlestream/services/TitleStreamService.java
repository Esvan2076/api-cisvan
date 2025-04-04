package com.cisvan.api.domain.titlestream.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.titlestream.TitleStreamRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleStreamService {

    private final TitleStreamRepository titleStreamRepository;

    // Obtener los streamings de un título
    public List<Integer> getStreamingsByTitleId(String tconst) {
        return titleStreamRepository.findStreamingIdsByTconst(tconst);
    }
}
