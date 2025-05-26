package com.cisvan.api.domain.trending;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrendingRepository extends JpaRepository<Trending, Long> {

    Optional<Trending> findByContentId(String TitleId);
    
    List<Trending> findTop20ByOrderByScoreDesc();

    
    List<Trending> findAllByScoreGreaterThan(int minScore);

    @Modifying
    @Query("""
        UPDATE Trending t
        SET t.historicalScore = t.historicalScore + t.score,
            t.score = 0,
            t.lastUpdate = CURRENT_TIMESTAMP
    """)
    void incrementHistoricalAndResetScores();

    @Query("""
        SELECT b.tconst, 0 AS defaultScore
        FROM Title b
        JOIN TitleRating r ON b.tconst = r.tconst
        WHERE b.titleType <> 'tvEpisode'
        AND b.startYear IN (2024, 2025)
        AND b.tconst NOT IN :excluded
        ORDER BY r.numVotes DESC
        LIMIT :limit
    """)
    List<Object[]> findFallbackTitlesForTrending(@Param("excluded") Set<String> excluded, @Param("limit") int limit);

    @Query(value = "SELECT generate_final_recommendations()", nativeQuery = true)
    void generateFinalRecommendations();
}