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

    private static final List<String> SPANISH_SPEAKING_REGIONS = List.of(
        "ES", "MX", "AR", "CL", "CO", "PE", "VE", "UY", "BO", "EC", "GT", "HN", "NI", "PA", "PY", "SV"
    );

    public void trySetSpanishTitle(TitleBasicDTO dto) {
        String tconst = dto.getTconst();
        List<Akas> allSpanishTitles = akasRepository.findByTconst(tconst);

        Optional<Akas> languageEs = allSpanishTitles.stream()
            .filter(this::isSpanishLanguage)
            .findFirst();

        if (languageEs.isPresent()) {
            dto.setPrimaryTitle(languageEs.get().getTitle());
            return;
        }

        Optional<Akas> regionEsFallback = allSpanishTitles.stream()
            .filter(this::isSpanishRegion)
            .findFirst();

        regionEsFallback.ifPresent(akas -> dto.setPrimaryTitle(akas.getTitle()));
    }

    private boolean isSpanishLanguage(Akas akas) {
        return "es".equalsIgnoreCase(akas.getLanguage());
    }

    private boolean isSpanishRegion(Akas akas) {
        String region = akas.getRegion();
        return region != null && SPANISH_SPEAKING_REGIONS.contains(region.toUpperCase());
    }
}