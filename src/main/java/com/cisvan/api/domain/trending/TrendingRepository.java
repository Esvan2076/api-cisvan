package com.cisvan.api.domain.trending;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendingRepository extends JpaRepository<Trending, Long> {

    List<Trending> findByContentId(String TitleId);
    
    List<Trending> findTop20ByOrderByScoreDesc();
}