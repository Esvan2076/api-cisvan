package com.cisvan.api.domain.title.dtos.searchDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MovieSearchResultDTO extends ContentSearchResultDTO {
    
    private boolean wasSearched = false;
}