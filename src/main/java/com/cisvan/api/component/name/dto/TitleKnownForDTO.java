package com.cisvan.api.component.name.dto;

import com.cisvan.api.component.ratings.TitleRatings;

import lombok.Data;

@Data
public class TitleKnownForDTO {
    
    private String tconst;
    private String titleType;
    private String primaryTitle;
    private Short startYear;
    private TitleRatings ratings;
    private String posterUrl;
}
