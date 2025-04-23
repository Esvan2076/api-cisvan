package com.cisvan.api.domain.title.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleDTO {
    
    private String tconst;
    private String titleType;
    private String primaryTitle;
    private String originalTitle;
    private Boolean isAdult;
    private Short startYear;
    private Short endYear;
    private Short runtimeMinutes;

    @Builder.Default
    private List<String> genres = new ArrayList<>();

    private String posterUrl;
}