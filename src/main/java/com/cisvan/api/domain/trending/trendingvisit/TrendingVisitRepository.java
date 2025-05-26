package com.cisvan.api.domain.trending.trendingvisit;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendingVisitRepository extends JpaRepository<TrendingVisit, Long> {
    boolean existsByUserIdAndContentIdAndVisitDate(Long userId, String contentId, LocalDate visitDate);
}
