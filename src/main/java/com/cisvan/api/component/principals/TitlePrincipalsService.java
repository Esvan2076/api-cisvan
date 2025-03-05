package com.cisvan.api.component.principals;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TitlePrincipalsService {
    @Autowired
    private TitlePrincipalsRepository titlePrincipalsRepository;

    public List<TitlePrincipals> findById(String tconst) {
        return titlePrincipalsRepository.findByTitleId(tconst);
    }
}
