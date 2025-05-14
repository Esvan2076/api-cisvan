package com.cisvan.api.domain.comment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tconst", length = 15)
    private String tconst;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    @Column(name = "reply_to_user_id")
    private Long replyToUserId;

    @Column(name = "comment_text", nullable = false, columnDefinition = "TEXT")
    private String commentText;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Builder.Default
    @Column(name = "last_notified_likes", nullable = false)
    private Integer lastNotifiedLikes = 0;

    @Builder.Default
    @Column(name = "is_review", nullable = false)
    private Boolean isReview = false;

    @Builder.Default
    @Column(name = "contains_spoiler", nullable = false)
    private Boolean containsSpoiler = false;

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
