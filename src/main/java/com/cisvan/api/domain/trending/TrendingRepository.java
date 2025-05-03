package com.cisvan.api.domain.trending;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendingRepository extends JpaRepository<Trending, Long> {

    Optional<Trending> findByContentId(String TitleId);
    
    List<Trending> findTop20ByOrderByScoreDesc();
}