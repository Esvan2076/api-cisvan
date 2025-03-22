package com.cisvan.api.component.titlestream;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TitleStreamService {
    @Autowired
    private TitleStreamRepository titleStreamRepository;

    // Obtener los streamings de un título
    public List<Integer> getStreamingsOfTitle(String tconst) {
        return titleStreamRepository.findStreamingIdsByTconst(tconst);
    }
}
