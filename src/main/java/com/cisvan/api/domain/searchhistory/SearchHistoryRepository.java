package com.cisvan.api.domain.searchhistory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    @Query("""
        SELECT DISTINCT sh FROM SearchHistory sh
        WHERE sh.userId = :userId
        ORDER BY sh.createdAt DESC
        LIMIT 10
    """)
    List<SearchHistory> findTop10ByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("""
        SELECT sh.resultId, sh.resultType, sh.resultTitle, COUNT(sh) as count,
               SUM(CASE 
                   WHEN sh.createdAt > :oneWeekAgo THEN 1.25
                   WHEN sh.createdAt > :twoWeeksAgo THEN 1.00
                   WHEN sh.createdAt > :twoMonthsAgo THEN 0.75
                   WHEN sh.createdAt > :sixMonthsAgo THEN 0.50
                   ELSE 0.25
               END) as weightedScore
        FROM SearchHistory sh
        WHERE sh.createdAt > :oneYearAgo
        GROUP BY sh.resultId, sh.resultType, sh.resultTitle
        ORDER BY weightedScore DESC
        LIMIT 10
    """)
    List<Object[]> findTop10TrendingSearches(
        @Param("oneWeekAgo") LocalDateTime oneWeekAgo,
        @Param("twoWeeksAgo") LocalDateTime twoWeeksAgo,
        @Param("twoMonthsAgo") LocalDateTime twoMonthsAgo,
        @Param("sixMonthsAgo") LocalDateTime sixMonthsAgo,
        @Param("oneYearAgo") LocalDateTime oneYearAgo
    );
}