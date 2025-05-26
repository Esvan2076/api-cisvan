package com.cisvan.api.domain.reviews.review;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.comment.Comment;
import com.cisvan.api.domain.comment.CommentRepository;
import com.cisvan.api.domain.reviews.dtos.ReviewResponseDTO;
import com.cisvan.api.domain.reviews.dtos.TitleReviewDTO;
import com.cisvan.api.domain.reviews.userGenresRating.UserGenresRating;
import com.cisvan.api.domain.reviews.userGenresRating.UserGenresRatingId;
import com.cisvan.api.domain.reviews.userGenresRating.UserGenresRatingRepository;
import com.cisvan.api.domain.reviews.userNameRating.UserNameRating;
import com.cisvan.api.domain.reviews.userNameRating.UserNameRatingId;
import com.cisvan.api.domain.reviews.userNameRating.UserNameRatingRepository;
import com.cisvan.api.domain.reviews.userTitleRating.UserTitleRating;
import com.cisvan.api.domain.reviews.userTitleRating.UserTitleRatingId;
import com.cisvan.api.domain.reviews.userTitleRating.UserTitleRatingRepository;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.trending.TrendingService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserTitleRatingRepository userTitleRatingRepository;
    private final UserGenresRatingRepository userGenresRatingRepository;
    private final UserNameRatingRepository userNameRatingRepository;
    private final TitleRepository titleRepository;
    private final ReviewDtoBuilder reviewDtoBuilder;
    private final TrendingService trendingService;

    @Transactional
    public Long createFullReview(TitleReviewDTO reviewDTO, Long userId) {
        // Verificar si el título existe en la base de datos
        if (!isValidTitleId(reviewDTO.getTconst())) {
            System.err.println("El título con tconst " + reviewDTO.getTconst() + " no existe en la base de datos o no es un título válido.");
            throw new IllegalArgumentException("El título especificado no existe o es un identificador de persona.");
        }

        // Verificar si ya existe un comentario de reseña del usuario para el mismo título
        Optional<Comment> existingCommentOpt = commentRepository.findByUserIdAndTconstAndIsReviewTrue(userId, reviewDTO.getTconst());
        if (existingCommentOpt.isPresent()) {
            Comment existingComment = existingCommentOpt.get();
            System.out.println("Eliminando comentario previo con ID: " + existingComment.getId());
            commentRepository.delete(existingComment);  // Elimina el comentario en cascada
        }

        System.out.println("Creando comentario para el título: " + reviewDTO.getTconst());

        // Crear el comentario base de la reseña
        Comment comment = Comment.builder()
                .userId(userId)
                .tconst(reviewDTO.getTconst())
                .commentText(reviewDTO.getCommentText())
                .isReview(true)
                .containsSpoiler(reviewDTO.isContainsSpoiler())
                .likeCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        System.out.println("Comentario guardado con ID: " + savedComment.getId());

        // Crear la reseña asociada al comentario
        Review review = Review.builder()
                .commentId(savedComment.getId())
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);
        System.out.println("Reseña guardada con ID: " + savedReview.getId());

        // Guardar la calificación del título
        UserTitleRating titleRating = UserTitleRating.builder()
                .id(new UserTitleRatingId(savedReview.getId(), reviewDTO.getTconst()))
                .userId(userId)
                .rating(reviewDTO.getScore())
                .createdAt(LocalDateTime.now())
                .build();

        userTitleRatingRepository.save(titleRating);
        System.out.println("Calificación del título guardada para el usuario: " + userId);

        // Guardar calificaciones adicionales (géneros, actores, directores)
        saveAdditionalRatings(savedReview, reviewDTO, userId);

        System.out.println("Reseña completa creada exitosamente con ID: " + savedReview.getId());

        // Evaluar si hay campos extra calificados
        boolean hasExtraRatings = !reviewDTO.getGenres().isEmpty() || !reviewDTO.getActors().isEmpty() || !reviewDTO.getDirectors().isEmpty();

        // Registrar puntos de tendencia
        trendingService.registerReviewPoints(userId, reviewDTO.getTconst(), hasExtraRatings);
        
        return savedReview.getId();
    }

    private boolean isValidTitleId(String tconst) {
        return tconst.startsWith("tt") && titleRepository.existsById(tconst);
    }

    private void saveAdditionalRatings(Review savedReview, TitleReviewDTO reviewDTO, Long userId) {
        // Guardar las calificaciones por género
        reviewDTO.getGenres().forEach(genre -> {
            UserGenresRating genreRating = UserGenresRating.builder()
                    .id(new UserGenresRatingId(savedReview.getId(), reviewDTO.getTconst(), genre.getGenre()))
                    .userId(userId)
                    .rating(genre.getScore())
                    .createdAt(LocalDateTime.now())
                    .build();
            userGenresRatingRepository.save(genreRating);
            System.out.println("Calificación guardada para el género: " + genre.getGenre());
        });

        // Guardar las calificaciones por actor
        reviewDTO.getActors().forEach(actor -> {
            UserNameRating actorRating = UserNameRating.builder()
                    .id(new UserNameRatingId(savedReview.getId(), actor.getNconst()))
                    .userId(userId)
                    .rating(actor.getScore())
                    .createdAt(LocalDateTime.now())
                    .build();
            userNameRatingRepository.save(actorRating);
            System.out.println("Calificación guardada para el actor: " + actor.getNconst());
        });

        // Guardar las calificaciones por director
        reviewDTO.getDirectors().forEach(director -> {
            UserNameRating directorRating = UserNameRating.builder()
                    .id(new UserNameRatingId(savedReview.getId(), director.getNconst()))
                    .userId(userId)
                    .rating(director.getScore())
                    .createdAt(LocalDateTime.now())
                    .build();
            userNameRatingRepository.save(directorRating);
            System.out.println("Calificación guardada para el director: " + director.getNconst());
        });
    }
        
    public Page<ReviewResponseDTO> getPaginatedReviewByLikes(int page, String tconst, Long paraUserId) {
        Pageable pageable = PageRequest.of(page, 1, Sort.by(Sort.Direction.DESC, "likeCount"));
        Page<Comment> commentsPage = commentRepository.findByTconstAndIsReviewTrueOrderByLikeCountDesc(tconst, pageable);
        Pageable usedPageable = pageable;

        if (!commentsPage.hasContent()) {
            if (commentsPage.getTotalPages() > 0) {
                int lastPage = commentsPage.getTotalPages() - 1;
                usedPageable = PageRequest.of(lastPage, 1, Sort.by(Sort.Direction.DESC, "likeCount"));
                commentsPage = commentRepository.findByTconstAndIsReviewTrueOrderByLikeCountDesc(tconst, usedPageable);
            } else {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }
        }

        if (!commentsPage.hasContent()) {
            return new PageImpl<>(Collections.emptyList(), usedPageable, commentsPage.getTotalElements());
        }

        Comment comment = commentsPage.getContent().get(0);
        Optional<Review> reviewOpt = reviewRepository.findByCommentId(comment.getId());

        if (reviewOpt.isEmpty()) {
            System.err.println("No se encontró Review para Comment ID: " + comment.getId());
            return new PageImpl<>(Collections.emptyList(), usedPageable, commentsPage.getTotalElements());
        }

        Review review = reviewOpt.get();
        ReviewResponseDTO dto = reviewDtoBuilder.buildFromReview(review, paraUserId);

        return new PageImpl<>(List.of(dto), usedPageable, commentsPage.getTotalElements());
    }

    public Page<ReviewResponseDTO> getPaginatedReviewsByUser(int page, Long targetUserId, Long viewerUserId) {
        Pageable pageable = PageRequest.of(page, 1, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviews = reviewRepository.findByUserId(targetUserId, pageable);

        if (!reviews.hasContent()) {
            return new PageImpl<>(Collections.emptyList(), pageable, reviews.getTotalElements());
        }

        List<ReviewResponseDTO> dtoList = reviews.stream()
                .map(review -> reviewDtoBuilder.buildFromReview(review, viewerUserId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, reviews.getTotalElements());
    }
}
