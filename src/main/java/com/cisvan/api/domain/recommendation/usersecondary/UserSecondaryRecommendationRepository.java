package com.cisvan.api.domain.recommendation.usersecondary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface UserSecondaryRecommendationRepository extends JpaRepository<UserSecondaryRecommendation, Long> {

    /**
     * Llama al procedimiento almacenado de PostgreSQL para actualizar/recalcular
     * las recomendaciones secundarias para un usuario específico.
     * @param userId El ID del usuario.
     * @param topN El número de recomendaciones secundarias a generar (ej. 30).
     */
    @Transactional // Los procedimientos que modifican datos deben ser transaccionales
    @Procedure(procedureName = "update_user_secondary_recommendations_proc") // Nombre exacto del PROCEDURE
    void executeUpdateUserSecondaryRecommendations(@Param("p_user_id") Long userId, @Param("p_top_n") int topN);

    /**
     * Método para obtener las recomendaciones secundarias de un usuario, ordenadas por su rango.
     */
    List<UserSecondaryRecommendation> findByUserIdOrderByRankForUserAsc(Long userId);
}