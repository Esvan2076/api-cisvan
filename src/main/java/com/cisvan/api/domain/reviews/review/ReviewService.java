package com.cisvan.api.domain.reviews.review;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
        Pageable pageable = PageRequest.of(page, 1, Sort.by(Sort.Direction.DESC, "likeCount"));
    
        // Obtener la página solicitada de comentarios
        Page<Comment> commentsPage = commentRepository.findByTconstAndIsReviewTrueOrderByLikeCountDesc(tconst, pageable);
    
        // Si la página está vacía, obtener la última página disponible
        if (!commentsPage.hasContent()) {
            int lastPage = commentsPage.getTotalPages() > 0 ? commentsPage.getTotalPages() - 1 : 0;
            pageable = PageRequest.of(lastPage, 1, Sort.by(Sort.Direction.DESC, "likeCount"));
            commentsPage = commentRepository.findByTconstAndIsReviewTrueOrderByLikeCountDesc(tconst, pageable);
        }
    
        // Obtener el comentario de la página actual
        Comment comment = commentsPage.getContent().get(0);
    
        // Buscar el review asociado al comentario
        Optional<Review> reviewOpt = reviewRepository.findByCommentId(comment.getId());
        if (reviewOpt.isEmpty()) {
            throw new NoSuchElementException("No se encontró una reseña para el comentario con ID: " + comment.getId());
        }
        Review review = reviewOpt.get();
    
        // Obtener el nombre del título desde el repositorio
        String titleName = titleRepository.findPrimaryTitleByTconst(tconst)
                .orElse("Título desconocido");
    
        // Obtener el score desde la entidad UserTitleRating utilizando el reviewId
        BigDecimal score = userTitleRatingRepository.findById_ReviewId(review.getId())
                .map(UserTitleRating::getRating)
                .orElse(BigDecimal.ZERO);
    
        // Obtener el usuario del comentario (si está disponible) incluyendo datos de prestigio
        UserSummaryPrestigeDTO userDto = null;
        if (comment.getUserId() != null) {
            Long commentUserId = comment.getUserId();
            userDto = userRepository.findById(commentUserId)
                    .map(user -> {
                        // Obtener datos de UserPrestige
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
    
        // Obtener el estado de "like" por el usuario actual
        boolean likedByMe = commentLikeRepository.existsById_UserIdAndId_CommentId(paraUserId, comment.getId());
    
        // Obtener la cantidad de respuestas de forma recursiva
        int replyCount = commentService.countRepliesRecursively(comment.getId());
    
        // Obtener los UserNameRating por reviewId
        List<UserNameRating> userNameRatings = userNameRatingRepository.findById_ReviewId(review.getId());
        List<String> nconsts = userNameRatings.stream()
                .map(rating -> rating.getId().getNconst())
                .collect(Collectors.toList());
    
        // Obtener actores y directores de la consulta de nombres
        List<Name> names = nameRepository.findByNconstIn(nconsts);
    
        // Separar actores y directores
        List<ReviewResponseDTO.ActorRatingDTO> actorRatings = new ArrayList<>();
        List<ReviewResponseDTO.DirectorRatingDTO> directorRatings = new ArrayList<>();
    
        for (UserNameRating rating : userNameRatings) {
            String nconst = rating.getId().getNconst();
            String primaryName = names.stream()
                    .filter(name -> name.getNconst().equals(nconst))
                    .map(Name::getPrimaryName)
                    .findFirst()
                    .orElse("Desconocido");
    
            // Verificar el tipo de profesión y agregar al DTO correspondiente
            for (Name name : names) {
                if (name.getNconst().equals(nconst)) {
                    if (name.getPrimaryProfession().contains("actor")) {
                        actorRatings.add(new ReviewResponseDTO.ActorRatingDTO(primaryName, rating.getRating()));
                    }
                    if (name.getPrimaryProfession().contains("director")) {
                        directorRatings.add(new ReviewResponseDTO.DirectorRatingDTO(primaryName, rating.getRating()));
                    }
                }
            }
        }
    
        // Obtener géneros desde la entidad UserGenresRating usando el reviewId
        List<UserGenresRating> genreRatings = userGenresRatingRepository.findById_ReviewId(review.getId());
        List<ReviewResponseDTO.GenreRatingDTO> genres = genreRatings.stream()
                .map(gr -> new ReviewResponseDTO.GenreRatingDTO(gr.getId().getGenre(), gr.getRating()))
                .collect(Collectors.toList());
    
        // Crear el objeto CommentContentDTO
        ReviewResponseDTO.CommentContentDTO commentDTO = ReviewResponseDTO.CommentContentDTO.builder()
                .id(comment.getId() != null ? comment.getId() : 0L)
                .commentText(comment.getCommentText())
                .likeCount(comment.getLikeCount())
                .containsSpoiler(comment.getContainsSpoiler())
                .createdAt(comment.getCreatedAt())
                .user(userDto)
                .likedByMe(likedByMe)  // Actualización: indicar si fue "liked" por el usuario actual
                .replyCount(replyCount) // Actualización: incluir el número de respuestas recursivamente
                .build();
    
        // Crear el objeto ReviewResponseDTO con los datos completos
        ReviewResponseDTO reviewResponseDTO = ReviewResponseDTO.builder()
                .reviewId(review.getId())
                .comment(commentDTO)
                .titleName(titleName)
                .score(score) 
                .genres(genres)  
                .actors(actorRatings)  
                .directors(directorRatings)  
                .build();
    
        // Devolver una página con el único elemento encontrado
        return new PageImpl<>(List.of(reviewResponseDTO), pageable, commentsPage.getTotalElements());
    }       
}
