package com.cisvan.api.component.akas;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TitleAkasService {
    @Autowired
    private TitleAkasRepository titleAkasRepository;

    public List<TitleAkas> findById(String tconst) {
        return titleAkasRepository.findByTitleId(tconst);
    }
}
