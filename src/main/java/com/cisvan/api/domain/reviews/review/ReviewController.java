package com.cisvan.api.domain.reviews.review;

import java.util.Collections;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cisvan.api.domain.reviews.dtos.ReviewResponseDTO;
import com.cisvan.api.domain.reviews.dtos.TitleReviewDTO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewOrchestrator reviewOrchestrator;

    @PostMapping("/submit")
    public ResponseEntity<?> createReview(@RequestBody TitleReviewDTO reviewDTO, HttpServletRequest request) {
        try {
            Long reviewId = reviewOrchestrator.createReview(reviewDTO, request);
            return ResponseEntity.ok(Collections.singletonMap("reviewId", reviewId));
        } catch (Exception e) {
            e.printStackTrace();  // ✅ Log completo del error en consola
            System.err.println("Error al crear la reseña: " + e.getMessage());  // ✅ Log con el mensaje del error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Ocurrió un error al crear la reseña: " + e.getMessage()));
        }
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<ReviewResponseDTO>> getPaginatedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam String tconst,
            HttpServletRequest request) {
        try {
            // Obtener la página de reseñas desde el orchestrator
            Page<ReviewResponseDTO> reviews = reviewOrchestrator.getPaginatedReviews(page, tconst, request);
            return ResponseEntity.ok(reviews);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Page.empty());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }    
}
