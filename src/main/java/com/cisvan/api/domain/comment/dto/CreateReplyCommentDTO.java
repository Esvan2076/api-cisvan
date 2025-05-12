package com.cisvan.api.domain.comment.dto;

import com.cisvan.api.validation.groups.BasicChecks;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@GroupSequence({CreateReplyCommentDTO.class, BasicChecks.class})
public class CreateReplyCommentDTO {

    @NotNull(message = "{NotNull}")
    private Long parentCommentId;

    @NotNull(message = "{NotNull}")
    private Long replyToUserId;

    @NotBlank(message = "{NotBlank}")
    @Size(groups = BasicChecks.class, max = 4096, message = "{SizeMax}")
    private String commentText;

    private Boolean containsSpoiler = false;
}
