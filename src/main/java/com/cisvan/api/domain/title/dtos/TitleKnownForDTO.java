package com.cisvan.api.domain.title.dtos;

import com.cisvan.api.domain.rating.Rating;

import lombok.Data;

@Data
public class TitleKnownForDTO {
    
    private String tconst;
    private String titleType;
    private String primaryTitle;
    private Short startYear;
    private Rating ratings;
    private String posterUrl;
}