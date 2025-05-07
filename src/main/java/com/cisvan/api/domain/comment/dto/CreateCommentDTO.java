package com.cisvan.api.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.cisvan.api.validation.groups.BasicChecks;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
@GroupSequence({CreateCommentDTO.class, BasicChecks.class})
public class CreateCommentDTO {

    private String tconst; // puede ser nulo

    private Long parentCommentId; // puede ser nulo

    @NotBlank(message = "{NotBlank}")
    @Size(groups = BasicChecks.class, max = 4096, message = "{SizeMax}")
    private String commentText;

    private Boolean containsSpoiler = false;

    // Validación personalizada
    @AssertTrue(message = "{TconstOrParentRequired}")
    public boolean isEitherTconstOrParentPresent() {
        return (tconst != null && !tconst.isBlank()) || parentCommentId != null;
    }
}
