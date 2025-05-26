package com.cisvan.api.domain.recommendation.userfinalrecommendation;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFinalRecommendationRepository extends JpaRepository<UserFinalRecommendation, Long> {

    List<UserFinalRecommendation> findAllByUserIdOrderByRankForUserAsc(Long userId);

    void deleteAllByUserId(Long userId);

    List<UserFinalRecommendation> findByUserIdOrderByRankForUserAsc(Long userId);
}