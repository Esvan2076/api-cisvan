package com.cisvan.api.domain.title.dtos;

import lombok.Data;

@Data
public class PrincipalTitleDTO { 

    private String primaryTitle;
    private Short startYear;
    private Short endYear;
}