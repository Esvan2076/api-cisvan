package com.cisvan.api.domain.reviews.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TitleOrderingDTO {
    
    private String tconst;
    private Short ordering;
}
