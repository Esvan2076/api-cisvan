package com.cisvan.api.domain.reviews.review;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import com.cisvan.api.domain.comment.CommentService;
import com.cisvan.api.domain.commentLike.CommentLikeRepository;
import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.repos.NameRepository;
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
import com.cisvan.api.domain.userprestige.UserPrestige;
import com.cisvan.api.domain.userprestige.UserPrestigeRepository;
import com.cisvan.api.domain.users.UsersRepository;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;

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
    private final UserPrestigeRepository userPrestigeRepository;
    private final UsersRepository userRepository;
    private final NameRepository nameRepository;
    private final CommentService commentService;
    private final CommentLikeRepository commentLikeRepository;

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
        Pageable initialPageable = PageRequest.of(page, 1, Sort.by(Sort.Direction.DESC, "likeCount"));
        Page<Comment> commentsPage = commentRepository.findByTconstAndIsReviewTrueOrderByLikeCountDesc(tconst, initialPageable);
        Pageable finalPageableUsed = initialPageable; // Para usar en el PageImpl final

        // Si la página solicitada inicialmente no tiene contenido
        if (!commentsPage.hasContent()) {
            if (commentsPage.getTotalPages() > 0) {
                // Hay comentarios en total, pero la página solicitada estaba vacía (quizás fuera de rango).
                // Ir a la última página que sí tiene contenido.
                int lastActualPage = commentsPage.getTotalPages() - 1;
                finalPageableUsed = PageRequest.of(lastActualPage, 1, Sort.by(Sort.Direction.DESC, "likeCount"));
                commentsPage = commentRepository.findByTconstAndIsReviewTrueOrderByLikeCountDesc(tconst, finalPageableUsed);
            } else {
                // No hay ningún comentario de reseña para este tconst.
                return new PageImpl<>(Collections.emptyList(), initialPageable, 0);
            }
        }

        // Después de cualquier ajuste, si sigue sin contenido, no hay nada que procesar.
        if (!commentsPage.hasContent()) {
            // Esto podría ocurrir si getTotalPages() > 0 pero la última página está vacía (poco probable si pageSize=1 y hay elementos)
            // O simplemente como una doble verificación.
            return new PageImpl<>(Collections.emptyList(), finalPageableUsed, commentsPage.getTotalElements());
        }

        // AHORA es seguro acceder a getContent().get(0), esta es la antigua línea 176
        Comment comment = commentsPage.getContent().get(0);

        Optional<Review> reviewOpt = reviewRepository.findByCommentId(comment.getId());
        if (reviewOpt.isEmpty()) {
            // Esto indica un problema de integridad de datos si un comentario de reseña existe sin su entidad Review.
            // Considera loggear un error más formal.
            System.err.println("Error de integridad de datos: No se encontró Review para Comment ID: " + comment.getId());
            // Devuelve una página vacía consistente con la firma del método.
            return new PageImpl<>(Collections.emptyList(), finalPageableUsed, commentsPage.getTotalElements());
        }
        Review review = reviewOpt.get();

        String titleName = titleRepository.findPrimaryTitleByTconst(tconst)
                .orElse("Título desconocido");

        BigDecimal score = userTitleRatingRepository.findById_ReviewId(review.getId())
                .map(UserTitleRating::getRating)
                .orElse(BigDecimal.ZERO);

        UserSummaryPrestigeDTO userDto = null;
        if (comment.getUserId() != null) {
            Long commentUserId = comment.getUserId();
            userDto = userRepository.findById(commentUserId)
                    .map(user -> {
                        Optional<UserPrestige> prestigeOpt = userPrestigeRepository.findById(commentUserId);
                        short currentRank = prestigeOpt.map(UserPrestige::getCurrentRank).orElse((short) 0);
                        String trendDirection = prestigeOpt.map(UserPrestige::getTrendDirection).orElse(null);
                        return UserSummaryPrestigeDTO.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .profileImageUrl(user.getProfileImageUrl())
                                .currentRank(currentRank)
                                .trendDirection(trendDirection)
                                .build();
                    })
                    .orElse(null);
        }

        boolean likedByMe = commentLikeRepository.existsById_UserIdAndId_CommentId(paraUserId, comment.getId());
        int replyCount = commentService.countRepliesRecursively(comment.getId());

        List<UserNameRating> userNameRatings = userNameRatingRepository.findById_ReviewId(review.getId());
        List<String> nconsts = userNameRatings.stream()
                .map(rating -> rating.getId().getNconst())
                .collect(Collectors.toList());

        List<Name> names = nconsts.isEmpty() ? Collections.emptyList() : nameRepository.findByNconstIn(nconsts);

        List<ReviewResponseDTO.ActorRatingDTO> actorRatings = new ArrayList<>();
        List<ReviewResponseDTO.DirectorRatingDTO> directorRatings = new ArrayList<>();

        for (UserNameRating rating : userNameRatings) {
            String nconst = rating.getId().getNconst();
            Optional<Name> nameOpt = names.stream()
                    .filter(name -> name.getNconst().equals(nconst))
                    .findFirst();
            
            String primaryName = nameOpt.map(Name::getPrimaryName).orElse("Desconocido");
            List<String> professions = nameOpt.map(Name::getPrimaryProfession).orElse(Collections.emptyList());

            // Usar equalsIgnoreCase para ser más robusto con los nombres de profesión si es necesario
            if (professions.stream().anyMatch(p -> p.equalsIgnoreCase("actor") || p.equalsIgnoreCase("actress"))) {
                actorRatings.add(new ReviewResponseDTO.ActorRatingDTO(primaryName, rating.getRating()));
            }
            if (professions.stream().anyMatch(p -> p.equalsIgnoreCase("director"))) {
                directorRatings.add(new ReviewResponseDTO.DirectorRatingDTO(primaryName, rating.getRating()));
            }
        }

        List<UserGenresRating> genreRatingsDb = userGenresRatingRepository.findById_ReviewId(review.getId());
        List<ReviewResponseDTO.GenreRatingDTO> genres = genreRatingsDb.stream()
                .map(gr -> new ReviewResponseDTO.GenreRatingDTO(gr.getId().getGenre(), gr.getRating()))
                .collect(Collectors.toList());

        ReviewResponseDTO.CommentContentDTO commentDTO = ReviewResponseDTO.CommentContentDTO.builder()
                .id(comment.getId() != null ? comment.getId() : 0L)
                .commentText(comment.getCommentText())
                .likeCount(comment.getLikeCount())
                .containsSpoiler(comment.getContainsSpoiler())
                .createdAt(comment.getCreatedAt())
                .user(userDto)
                .likedByMe(likedByMe)
                .replyCount(replyCount)
                .build();

        ReviewResponseDTO reviewResponseDTO = ReviewResponseDTO.builder()
                .reviewId(review.getId())
                .comment(commentDTO)
                .titleName(titleName)
                .score(score)
                .genres(genres)
                .actors(actorRatings)
                .directors(directorRatings)
                .build();

        // Usar el Pageable que realmente se utilizó para obtener el contenido
        return new PageImpl<>(List.of(reviewResponseDTO), finalPageableUsed, commentsPage.getTotalElements());
    }
}
