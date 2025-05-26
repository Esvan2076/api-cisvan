package com.cisvan.api.domain.name.dto;

import com.cisvan.api.domain.title.dtos.PrincipalTitleDTO;

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
public class NameSearchResultDTO {
    
    private String nconst;
    private String primaryName;
    private String primaryProfession;
    private PrincipalTitleDTO principalTitle;

    @Builder.Default
    private boolean wasSearched = false;
}