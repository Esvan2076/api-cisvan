package com.cisvan.api.domain.title.dtos;

import com.cisvan.api.domain.titlerating.TitleRating;

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
public class TitleKnownForDTO {
    
    private String tconst;
    private String titleType;
    private String primaryTitle;
    private Short startYear;
    private TitleRating titleRatings;
    private String posterUrl;
}
