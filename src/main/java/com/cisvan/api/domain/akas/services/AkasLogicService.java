package com.cisvan.api.domain.akas.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.akas.Akas;
import com.cisvan.api.domain.akas.AkasRepository;
import com.cisvan.api.domain.title.dtos.TitleBasicDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AkasLogicService {

    private final AkasRepository akasRepository;

    public void overrideTitleInSpanish(TitleBasicDTO dto) {
        String tconst = dto.getTconst();
    
        List<Akas> allSpanishTitles = akasRepository.findByTconst(tconst);
    
        Optional<Akas> languageEs = allSpanishTitles.stream()
            .filter(a -> "es".equalsIgnoreCase(a.getLanguage()))
            .findFirst();
    
        if (languageEs.isPresent()) {
            dto.setPrimaryTitle(languageEs.get().getTitle());
            return;
        }
    
        Optional<Akas> regionEsFallback = allSpanishTitles.stream()
            .filter(a -> {
                String region = a.getRegion();
                return region != null && List.of(
                    "ES", "MX", "AR", "CL", "CO", "PE", "VE", "UY", "BO", "EC", "GT", "HN", "NI", "PA", "PY", "SV"
                ).contains(region.toUpperCase());
            })
            .findFirst();
    
        regionEsFallback.ifPresent(akas -> dto.setPrimaryTitle(akas.getTitle()));
    }    
}
