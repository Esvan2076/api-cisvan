package com.cisvan.api.domain.title.dtos;

import java.math.BigDecimal;

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
public class EpisodeSummaryDTO {
    
    private String tconst;
    private String primaryTitle;
    private Short episodeNumber;
    private BigDecimal averageRating;
    private Integer numVotes;
}