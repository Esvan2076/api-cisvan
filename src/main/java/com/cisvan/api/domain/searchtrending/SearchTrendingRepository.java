package com.cisvan.api.domain.searchtrending;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SearchTrendingRepository extends JpaRepository<SearchTrending, String> {
    
    List<SearchTrending> findTop10ByOrderByWeightedScoreDesc();
}