package com.cisvan.api.domain.title.dtos;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EpisodeSummaryDTO {
    
    private String tconst;
    private String primaryTitle;
    private Short episodeNumber;
    private BigDecimal averageRating;
    private Integer numVotes;
}