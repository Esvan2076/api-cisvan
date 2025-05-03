package com.cisvan.api.domain.userprestige.dtos;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPrestigeDTO {
    
    private short currentRank;
    private BigDecimal weightedScore;
    private String trendDirection;
}