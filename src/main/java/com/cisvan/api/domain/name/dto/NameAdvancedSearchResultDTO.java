package com.cisvan.api.domain.name.dto;

import java.util.List;

import lombok.Data;

@Data
public class NameAdvancedSearchResultDTO {

    private String nconst;
    private String primaryName;
    private Short birthYear;
    private List<String> primaryProfession;
    private String imageUrl;
}