package com.cisvan.api.domain.reviews.review;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

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

    public Long createReview(TitleReviewDTO reviewDTO, HttpServletRequest request) {
        // Obtener el usuario autenticado desde el token
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            throw new IllegalStateException("Usuario no autenticado");
        }

        Users user = userOpt.get();
        return reviewService.createFullReview(reviewDTO, user.getId());
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
}