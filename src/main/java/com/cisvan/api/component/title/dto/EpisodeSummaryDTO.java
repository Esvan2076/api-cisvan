package com.cisvan.api.component.title.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class EpisodeSummaryDTO {
    
    private Short episodeNumber;
    private String tconst;
    private String primaryTitle;
    private BigDecimal averageRating;
    private Integer numVotes;
}