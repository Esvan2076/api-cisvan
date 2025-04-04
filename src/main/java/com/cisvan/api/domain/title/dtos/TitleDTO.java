package com.cisvan.api.domain.title.dtos;

import java.util.List;

import lombok.Data;

@Data
public class TitleDTO {

    private String tconst;
    private String titleType;
    private String primaryTitle;
    private String originalTitle;
    private Boolean isAdult;
    private Short startYear;
    private Short endYear;
    private Short runtimeMinutes;
    private List<String> genres;
    private String posterUrl;
}
