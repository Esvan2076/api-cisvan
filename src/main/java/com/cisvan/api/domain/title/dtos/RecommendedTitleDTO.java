package com.cisvan.api.domain.title.dtos;

import com.cisvan.api.domain.titlerating.TitleRating;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedTitleDTO {

    private String tconst;
    private String titleType;
    private String primaryTitle;
    private Short startYear;
    private Short endYear;
    private String posterUrl;
    private TitleRating titleRating;
}