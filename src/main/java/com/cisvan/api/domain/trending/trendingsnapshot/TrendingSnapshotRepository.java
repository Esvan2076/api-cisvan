package com.cisvan.api.domain.trending.trendingsnapshot;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrendingSnapshotRepository extends JpaRepository<TrendingSnapshot, Long> {

    void deleteAllBySnapshotDate(LocalDate snapshotDate);
}