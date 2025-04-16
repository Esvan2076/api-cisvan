package com.cisvan.api.domain.name.dto;

import java.util.List;

import com.cisvan.api.domain.namerating.NameRating;

import lombok.Data;

@Data
public class NameBasicDTO {

    private String nconst;
    private String primaryName;
    private Short birthYear;
    private Short deathYear;
    private List<String> primaryProfession;
    private List<String> knownForTitles;
    private String imageUrl;
    private NameRating nameRatings;
}
