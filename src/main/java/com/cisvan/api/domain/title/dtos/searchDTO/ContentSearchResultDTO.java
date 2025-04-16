package com.cisvan.api.domain.title.dtos.searchDTO;

import lombok.Data;

@Data
public class ContentSearchResultDTO {

    private String tconst;
    private String primaryTitle;
    private Short startYear;
    private String actors;
}
