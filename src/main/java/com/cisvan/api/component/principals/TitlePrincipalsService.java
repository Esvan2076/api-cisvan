package com.cisvan.api.component.principals;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cisvan.api.component.name.NameBasicsRepository;
import com.cisvan.api.component.principals.dtos.CastMemberDTO;

@Service
public class TitlePrincipalsService {
    @Autowired
    private TitlePrincipalsRepository titlePrincipalsRepository;

    @Autowired
    private NameBasicsRepository nameBasicsRepository;


    public List<TitlePrincipals> findById(String tconst) {
        return titlePrincipalsRepository.findByTitleId(tconst);
    }

    public List<CastMemberDTO> getCastByTconst(String tconst) {
        List<TitlePrincipals> principals = titlePrincipalsRepository.findByIdTconst(tconst);
    
        return principals.stream()
            .filter(p -> {
                String category = p.getCategory();
                return "actor".equalsIgnoreCase(category) || "actress".equalsIgnoreCase(category);
            })
            .sorted(Comparator.comparing(p -> p.getId().getOrdering()))
            .map(p -> {
                CastMemberDTO dto = new CastMemberDTO();
                dto.setNconst(p.getNconst());
    
                nameBasicsRepository.findById(p.getNconst()).ifPresent(n -> {
                    dto.setPrimaryName(n.getPrimaryName());
                    dto.setImageUrl(n.getImageUrl()); // ← asigna la foto
                });
    
                dto.setCharacters(p.getCharacters());
                return dto;
            })
            .toList();
    }      
}
