package com.cisvan.api.domain.trending.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrendingScoreDTO {

    private Integer score;
    private Long historicalScore;
}
