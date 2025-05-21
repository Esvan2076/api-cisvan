package com.cisvan.api.domain.recommendation.userrecomendationitem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRecommendationItemRepository extends JpaRepository<UserRecommendationItem, Long> {
	
}
