package com.cisvan.api.domain.title.dtos;

import java.util.List;

import com.cisvan.api.domain.name.dto.NameEssencialDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TitleReviewDTO {

    private String tconst;
    private String primaryTitle;
    private List<String> genres;

    // Listas de actores y directores
    private List<NameEssencialDTO> actors;
    private List<NameEssencialDTO> directors;
}
