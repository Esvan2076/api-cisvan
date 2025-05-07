package com.cisvan.api.domain.comment;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.comment.dto.CommentResponseDTO;
import com.cisvan.api.domain.comment.dto.CreateCommentDTO;
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
        System.out.println("MENSAJE1");
    
        Optional<Comment> optionalComment = commentOrchestrator.createComment(createCommentDTO, request);


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
        List<CommentResponseDTO> comments = commentOrchestrator.getCommentsForTitle(tconst, request);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/title/{tconst}/reviews")
    public ResponseEntity<List<Comment>> getReviewsForTitle(@PathVariable String tconst) {
        return ResponseEntity.ok(commentService.getReviewsForTitle(tconst));
    }

    @GetMapping("/reply/{parentId}")
    public ResponseEntity<List<Comment>> getReplies(@PathVariable Long parentId) {
        return ResponseEntity.ok(commentService.getReplies(parentId));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpServletRequest request) {
        commentOrchestrator.deleteComment(commentId, request);
        return ResponseEntity.noContent().build(); // HTTP 204
    }
}