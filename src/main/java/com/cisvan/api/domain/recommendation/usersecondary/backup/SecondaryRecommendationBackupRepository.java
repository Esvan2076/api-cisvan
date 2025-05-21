package com.cisvan.api.domain.recommendation.usersecondary.backup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecondaryRecommendationBackupRepository extends JpaRepository<SecondaryRecommendationBackup, Long> {
    // Puedes añadir métodos de búsqueda personalizados aquí si los necesitas
}
