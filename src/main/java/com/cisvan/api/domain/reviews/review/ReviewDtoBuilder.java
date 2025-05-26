package com.cisvan.api.domain.reviews.review;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cisvan.api.domain.comment.Comment;
import com.cisvan.api.domain.comment.CommentRepository;
import com.cisvan.api.domain.comment.CommentService;
import com.cisvan.api.domain.commentLike.CommentLikeRepository;
import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.repos.NameRepository;
import com.cisvan.api.domain.reviews.dtos.ReviewResponseDTO;
import com.cisvan.api.domain.reviews.userGenresRating.UserGenresRating;
import com.cisvan.api.domain.reviews.userGenresRating.UserGenresRatingRepository;
import com.cisvan.api.domain.reviews.userNameRating.UserNameRating;
import com.cisvan.api.domain.reviews.userNameRating.UserNameRatingRepository;
import com.cisvan.api.domain.reviews.userTitleRating.UserTitleRating;
import com.cisvan.api.domain.reviews.userTitleRating.UserTitleRatingRepository;
import com.cisvan.api.domain.title.repos.TitleRepository;
import com.cisvan.api.domain.userprestige.UserPrestige;
import com.cisvan.api.domain.userprestige.UserPrestigeRepository;
import com.cisvan.api.domain.users.UsersRepository;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewDtoBuilder {

    private final CommentRepository commentRepository;
    private final TitleRepository titleRepository;
    private final UserTitleRatingRepository userTitleRatingRepository;
    private final UsersRepository userRepository;
    private final UserPrestigeRepository userPrestigeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentService commentService;
    private final UserNameRatingRepository userNameRatingRepository;
    private final NameRepository nameRepository;
    private final UserGenresRatingRepository userGenresRatingRepository;

    public ReviewResponseDTO buildFromReview(Review review, Long currentUserId) {
        Optional<Comment> commentOpt = commentRepository.findById(review.getCommentId());
        if (commentOpt.isEmpty()) return null;

        Comment comment = commentOpt.get();

        String titleName = titleRepository.findPrimaryTitleByTconst(comment.getTconst()).orElse("Título desconocido");

        BigDecimal score = userTitleRatingRepository.findById_ReviewId(review.getId())
                .map(UserTitleRating::getRating)
                .orElse(BigDecimal.ZERO);

        UserSummaryPrestigeDTO userDto = userRepository.findById(review.getUserId())
                .map(user -> {
                    Optional<UserPrestige> prestigeOpt = userPrestigeRepository.findById(user.getId());
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

        boolean likedByMe = commentLikeRepository.existsById_UserIdAndId_CommentId(currentUserId, comment.getId());
        int replyCount = commentService.countRepliesRecursively(comment.getId());

        List<UserNameRating> userNameRatings = userNameRatingRepository.findById_ReviewId(review.getId());
        List<String> nconsts = userNameRatings.stream().map(r -> r.getId().getNconst()).collect(Collectors.toList());
        List<Name> names = nameRepository.findByNconstIn(nconsts);

        List<ReviewResponseDTO.ActorRatingDTO> actors = new ArrayList<>();
        List<ReviewResponseDTO.DirectorRatingDTO> directors = new ArrayList<>();

        for (UserNameRating rating : userNameRatings) {
            String nconst = rating.getId().getNconst();
            Optional<Name> nameOpt = names.stream().filter(name -> name.getNconst().equals(nconst)).findFirst();
            String primaryName = nameOpt.map(Name::getPrimaryName).orElse("Desconocido");
            List<String> professions = nameOpt.map(Name::getPrimaryProfession).orElse(Collections.emptyList());

            if (professions.stream().anyMatch(p -> p.equalsIgnoreCase("actor") || p.equalsIgnoreCase("actress"))) {
                actors.add(new ReviewResponseDTO.ActorRatingDTO(primaryName, rating.getRating()));
            }
            if (professions.stream().anyMatch(p -> p.equalsIgnoreCase("director"))) {
                directors.add(new ReviewResponseDTO.DirectorRatingDTO(primaryName, rating.getRating()));
            }
        }

        List<UserGenresRating> genreRatingsDb = userGenresRatingRepository.findById_ReviewId(review.getId());
        List<ReviewResponseDTO.GenreRatingDTO> genres = genreRatingsDb.stream()
                .map(gr -> new ReviewResponseDTO.GenreRatingDTO(gr.getId().getGenre(), gr.getRating()))
                .collect(Collectors.toList());

        ReviewResponseDTO.CommentContentDTO commentDTO = ReviewResponseDTO.CommentContentDTO.builder()
                .id(comment.getId())
                .commentText(comment.getCommentText())
                .likeCount(comment.getLikeCount())
                .containsSpoiler(comment.getContainsSpoiler())
                .createdAt(comment.getCreatedAt())
                .user(userDto)
                .likedByMe(likedByMe)
                .replyCount(replyCount)
                .build();

        return ReviewResponseDTO.builder()
                .reviewId(review.getId())
                .comment(commentDTO)
                .titleName(titleName)
                .score(score)
                .genres(genres)
                .actors(actors)
                .directors(directors)
                .build();
    }
}
