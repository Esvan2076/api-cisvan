package com.cisvan.api.domain.name.dto;

import com.cisvan.api.domain.title.dtos.PrincipalTitleDTO;

import lombok.Data;

@Data
public class NameSearchResultDTO {
    
    private String nconst;
    private String primaryName;
    private String primaryProfession;
    private PrincipalTitleDTO principalTitle; 
}
