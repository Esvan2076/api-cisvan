package com.cisvan.api.domain.commentLike;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentLikeOrchestrator {

    private final CommentLikeService commentLikeService;
    private final UserLogicService userLogicService;

    public void likeComment(Long commentId, HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return;
        }

        commentLikeService.like(commentId, userOpt.get().getId());
    }

    public void unlikeComment(Long commentId, HttpServletRequest request) {
        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return;
        }

        commentLikeService.unlike(commentId, userOpt.get().getId());
    }
}