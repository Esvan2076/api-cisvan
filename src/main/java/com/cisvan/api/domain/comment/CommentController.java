package com.cisvan.api.domain.comment;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.comment.dto.CommentContentDTO;
import com.cisvan.api.domain.comment.dto.CommentResponseDTO;
import com.cisvan.api.domain.comment.dto.CreateCommentDTO;
import com.cisvan.api.domain.comment.dto.CreateReplyCommentDTO;
import com.cisvan.api.helper.ControllerHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final ControllerHelper controllerHelper;
    private final CommentOrchestrator commentOrchestrator;

    @PostMapping()
    public ResponseEntity<?> createComment(
        @Valid @RequestBody CreateCommentDTO createCommentDTO,
        HttpServletRequest request,
        BindingResult result
    ) {
        if (result.hasErrors()) {
            OperationResult errors = controllerHelper.validate(result);
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Comment> optionalComment = commentOrchestrator.createComment(createCommentDTO, request);

        if (optionalComment.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(optionalComment.get());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/reply")
    public ResponseEntity<?> createReplyComment(
        @Valid @RequestBody CreateReplyCommentDTO createReplyCommentDTO,
        HttpServletRequest request,
        BindingResult result
    ) {
        if (result.hasErrors()) {
            OperationResult errors = controllerHelper.validate(result);
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Comment> optionalComment = commentOrchestrator.createReplyComment(createReplyCommentDTO, request);

        if (optionalComment.isPresent()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(optionalComment.get());
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/title/{tconst}")
    public ResponseEntity<?> getCommentsForTitle(
        @PathVariable String tconst,
        HttpServletRequest request
        ) {
        List<CommentContentDTO> comments = commentOrchestrator.getCommentsForTitle(tconst, request);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/title/{tconst}/reviews")
    public ResponseEntity<List<Comment>> getReviewsForTitle(@PathVariable String tconst) {
        return ResponseEntity.ok(commentService.getReviewsForTitle(tconst));
    }

    @GetMapping("/replies/{parentCommentId}")
    public ResponseEntity<?> getRepliesForComment(
            @PathVariable Long parentCommentId,
            HttpServletRequest request
    ) {
        List<CommentResponseDTO> replies = commentOrchestrator.getRepliesForComment(parentCommentId, request);
        return ResponseEntity.ok(replies);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        commentOrchestrator.deleteComment(commentId, request);
        return ResponseEntity.noContent().build(); // HTTP 204
    }
}