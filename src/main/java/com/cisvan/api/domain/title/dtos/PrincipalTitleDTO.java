package com.cisvan.api.domain.title.dtos;

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
public class PrincipalTitleDTO {
    
    private String primaryTitle;
    private Short startYear;
    private Short endYear;
}