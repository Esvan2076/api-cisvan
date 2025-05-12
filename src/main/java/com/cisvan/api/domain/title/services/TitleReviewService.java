package com.cisvan.api.domain.title.services;

import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.name.dto.NameEssencialDTO;
import com.cisvan.api.domain.principal.repos.PrincipalRepository;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.TitleReviewDTO;
import com.cisvan.api.domain.title.repos.TitleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TitleReviewService {

    private final TitleRepository titleRepository;
    private final PrincipalRepository principalRepository;

    public Optional<TitleReviewDTO> getTitleReview(String tconst) {
        // Obtener el título básico
        Optional<Title> titleOpt = titleRepository.findById(tconst);
        if (titleOpt.isEmpty()) {
            return Optional.empty();
        }

        Title title = titleOpt.get();

        // Obtener actores y actrices
        List<NameEssencialDTO> actorsAndActresses = principalRepository.findActorsAndActressesByTconst(tconst, List.of("actor", "actress"));

        // Obtener directores
        // Obtener directores desde la consulta nativa
        List<Object[]> directorsRaw = principalRepository.findDirectorsByTconst(tconst);
        List<NameEssencialDTO> directors = directorsRaw.stream()
            .map(row -> new NameEssencialDTO((String) row[0], (String) row[1]))
            .collect(Collectors.toList());

        // Construir el DTO
        TitleReviewDTO dto = TitleReviewDTO.builder()
                .tconst(title.getTconst())
                .primaryTitle(title.getPrimaryTitle())
                .genres(title.getGenres())
                .actors(actorsAndActresses)
                .directors(directors)
                .build();

        return Optional.of(dto);
    }
}