package com.cisvan.api.domain.name.dto;

import java.util.List;

import com.cisvan.api.domain.namerating.NameRating;

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