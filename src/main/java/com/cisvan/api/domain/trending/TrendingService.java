package com.cisvan.api.domain.trending;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrendingService {
    
    private final TrendingRepository trendingRepository;

    public Optional<Trending> getTrendingById(Long trendingId) {
        return trendingRepository.findById(trendingId);
    }

    public List<Trending> getTrendingsByTitleId(String tconst) {
        return trendingRepository.findByContentId(tconst);
    }

    public List<Trending> getTopTrendingContents() {
        return trendingRepository.findTop20ByOrderByScoreDesc();
    }
}
