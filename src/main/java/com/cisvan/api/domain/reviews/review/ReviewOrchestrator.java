package com.cisvan.api.domain.reviews.review;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.cisvan.api.domain.notification.services.NotificationService;
import com.cisvan.api.domain.recommendation.RecommendationService;
import com.cisvan.api.domain.reviews.dtos.ReviewResponseDTO;
import com.cisvan.api.domain.reviews.dtos.TitleReviewDTO;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewOrchestrator {

    private final ReviewService reviewService;
    private final UserLogicService userLogicService;
    private final NotificationService notificationService;
    private final RecommendationService recommendationService;

    public Long createReview(TitleReviewDTO reviewDTO, HttpServletRequest request) {
        // Obtener el usuario autenticado desde el token
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        Users user = userOpt.get();
        // Crear la reseña
        Long reviewId = reviewService.createFullReview(reviewDTO, user.getId());

        // Ejecutar el algoritmo de recomendación en segundo plano
        recommendationService.triggerRecommendationAlgorithm(reviewDTO, user.getId());

        // Notificar a seguidores (ASÍNCRONO)
        notificationService.notifyFollowersOfNewReview(user.getId());

        return reviewId;
    }

    public Page<ReviewResponseDTO> getPaginatedReviews(int page, String tconst, HttpServletRequest request) {
        Long userId = null;
        // Obtener el usuario autenticado desde el token
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (!userOpt.isEmpty()) {
            Users user = userOpt.get();
            userId = user.getId();
        }

        // Llamada al servicio para obtener la página de reseñas
        return reviewService.getPaginatedReviewByLikes(page, tconst, userId);
    }

    public Page<ReviewResponseDTO> getUserPaginatedReviews(Long targetUserId, int page, HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            throw new NoSuchElementException("Usuario no autenticado");
        }

        Long viewerUserId = userOpt.get().getId(); // Se usa para likedByMe

        return reviewService.getPaginatedReviewsByUser(page, targetUserId, viewerUserId);
    }
}