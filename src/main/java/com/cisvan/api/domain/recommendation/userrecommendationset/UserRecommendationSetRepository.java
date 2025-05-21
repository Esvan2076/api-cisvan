package com.cisvan.api.domain.recommendation.userrecommendationset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRecommendationSetRepository extends JpaRepository<UserRecommendationSet, Long> {

    long countByUserId(Long userId);
}