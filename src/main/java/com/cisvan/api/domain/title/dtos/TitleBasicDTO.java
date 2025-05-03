package com.cisvan.api.domain.title.dtos;

import java.util.List;

import com.cisvan.api.domain.name.dto.NameEssencialDTO;
import com.cisvan.api.domain.streaming.Streaming;
import com.cisvan.api.domain.titlerating.TitleRating;
import com.cisvan.api.domain.trending.dtos.TrendingScoreDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TitleBasicDTO extends TitleDTO {

    private TitleRating titleRatings;
    private List<NameEssencialDTO> directos;
    private List<NameEssencialDTO> writers;
    private List<Streaming> streamingServices;

    private TrendingScoreDTO trendingScore;
}
