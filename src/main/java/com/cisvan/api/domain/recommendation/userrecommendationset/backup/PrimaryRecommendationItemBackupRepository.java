package com.cisvan.api.domain.recommendation.userrecommendationset.backup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrimaryRecommendationItemBackupRepository extends JpaRepository<PrimaryRecommendationItemBackup, Long> {

}
