package com.cisvan.api.domain.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.users.dto.request.AuthRequest;
import com.cisvan.api.domain.users.dto.request.EmailRequest;
import com.cisvan.api.domain.users.dto.request.LoginRequest;
import com.cisvan.api.domain.users.dto.request.ProfileUrl;
import com.cisvan.api.domain.users.dto.request.ResetPasswordRequest;
import com.cisvan.api.domain.users.dto.request.VerificationCodeRequest;
import com.cisvan.api.domain.users.dto.response.EmailVerificationResponse;
import com.cisvan.api.domain.users.dto.response.NotificationPromptStatusDTO;
import com.cisvan.api.domain.users.dto.response.UserProfileDTO;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeExtendedDTO;
import com.cisvan.api.domain.users.services.UserService;
import com.cisvan.api.helper.ControllerHelper;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final ControllerHelper controllerHelper;
    private final UserOrchestrator userOrchestrator;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request, BindingResult result) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);

        Optional<EmailVerificationResponse> response = userOrchestrator.register(request, operationResult);
        if (operationResult.hasErrors()) return ResponseEntity.unprocessableEntity().body(operationResult);

        return ResponseEntity.status(HttpStatus.CREATED).body(response.get());
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendEmailVerificationCode(
        @Valid @RequestBody EmailRequest request,
        BindingResult result
    ) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);
    
        Optional<EmailVerificationResponse> response = userOrchestrator.resendEmailVerificationCode(
            request.getEmail(), operationResult
        );
    
        if (operationResult.hasErrors()) {
            return ResponseEntity.unprocessableEntity().body(operationResult);
        }
    
        return ResponseEntity.ok(response.get());
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmailByCode(
        @Valid @RequestBody VerificationCodeRequest request,
        BindingResult result
    ) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) {
            return ResponseEntity.badRequest().body(operationResult);
        }
    
        Optional<String> jwtOpt = userOrchestrator.verifyEmail(request, operationResult);
    
        if (operationResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(operationResult);
        }
    
        return ResponseEntity.ok(jwtOpt.get());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult result) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);
    
        Optional<String> jwtOpt = userOrchestrator.login(request, operationResult);
        if (operationResult.hasErrors()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(operationResult);
    
        return ResponseEntity.ok(jwtOpt.get());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
        @Valid @RequestBody EmailRequest request,
        BindingResult result
    ) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);
    
        Optional<EmailVerificationResponse> emailOpt = userOrchestrator.forgotPassword(request.getEmail(), operationResult);
        if (operationResult.hasErrors()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(operationResult);
    
        return ResponseEntity.ok(emailOpt.get());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request,
        BindingResult result
    ) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);
    
        boolean success = userOrchestrator.resetPassword(request, operationResult);
    
        if (!success) {
            return ResponseEntity.badRequest().body(operationResult);
        }
    
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpServletRequest request) {
        return userOrchestrator.getMe(request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @GetMapping("/profile")
    public ResponseEntity<?> fetchProfile(HttpServletRequest request) {
        Optional<UserProfileDTO> profileOpt = userOrchestrator.getProfile(request);
        
        return controllerHelper.handleOptional(profileOpt);
    }

    @GetMapping("/{userId}/page")
    public ResponseEntity<?> fetchMainPage(HttpServletRequest request, @PathVariable Long userId)  {
        Optional<UserSummaryPrestigeExtendedDTO> userOpt = userOrchestrator.getMainOfUser(request, userId);
        return controllerHelper.handleOptional(userOpt);
    }

    @PostMapping("/{targetUserId}/toggle-follow")
    public ResponseEntity<?> changeFollow(HttpServletRequest request, @PathVariable Long targetUserId)  {
        boolean nowFollowing = userOrchestrator.toggleFollow(request, targetUserId);

        if(!nowFollowing) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/activate-notification")
    public ResponseEntity<?> activateEmailNotification(HttpServletRequest request) {
        boolean notificationsEnabled = userOrchestrator.activateEmailNotification(request);

        return ResponseEntity.ok().body(
            Collections.singletonMap("notificationsEnabled", notificationsEnabled)
        );
    }

    @PostMapping("/deactivate-notification")
    public ResponseEntity<?> deactivateEmailNotification(HttpServletRequest request) {
        boolean notificationsEnabled = userOrchestrator.deactivateEmailNotification(request);

        return ResponseEntity.ok().body(
            Collections.singletonMap("notificationsEnabled", notificationsEnabled)
        );
    }

    @GetMapping("/followers")
    public ResponseEntity<?> fetchFollowers(HttpServletRequest request)  {
        List<UserSummaryPrestigeDTO> followers = userOrchestrator.getFollowers(request);
        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following")
    public ResponseEntity<?> fetchFollowing(HttpServletRequest request)  {
        List<UserSummaryPrestigeDTO> followers = userOrchestrator.getFollowing(request);
        return ResponseEntity.ok(followers);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> updateProfileImage(
        HttpServletRequest request,
        @RequestParam("file") MultipartFile file
    ) {
        OperationResult operationResult = new OperationResult();

        Optional<String> response = userOrchestrator.updateProfileImage(request, file, operationResult);
        if (operationResult.hasErrors()) return ResponseEntity.unprocessableEntity().body(operationResult);

        return ResponseEntity.ok(Collections.singletonMap("profileImageUrl", response.get()));
    }

    @PutMapping("/update-image-url")
    public ResponseEntity<?> updateProfileImageFromUrl(
        HttpServletRequest request,
        @RequestBody @Valid ProfileUrl updateRequest
    ) {
        OperationResult operationResult = new OperationResult();

        Optional<ProfileUrl> response = userOrchestrator.updateProfileImageFromUrl(request, updateRequest.getImageUrl(), operationResult);
        if (operationResult.hasErrors()) return ResponseEntity.unprocessableEntity().body(operationResult);

        return ResponseEntity.ok(response.get());
    }

    @DeleteMapping("/profile-image")
    public ResponseEntity<?> deleteProfileImage(HttpServletRequest request) {
        OperationResult operationResult = new OperationResult();

        userOrchestrator.deleteProfileImage(request, operationResult);
        if (operationResult.hasErrors()) return ResponseEntity.unprocessableEntity().body(operationResult);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/notification-prompt-status")
    public ResponseEntity<NotificationPromptStatusDTO> getNotificationPromptStatus(HttpServletRequest request) {
        NotificationPromptStatusDTO status = userOrchestrator.getNotificationPromptStatus(request);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserSummaryPrestigeDTO>> searchUsers(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok(userOrchestrator.searchUsersByUsername(username, page));
    }

    @PutMapping("/{userId}/ban-toggle")
    public ResponseEntity<?> toggleBanUser(
        @PathVariable Long userId
    ) {
        boolean isBanned = userService.toggleBanUser(userId);
        return ResponseEntity.ok(Collections.singletonMap("banned", isBanned));
    }

    @GetMapping("/banned")
    public ResponseEntity<List<UserSummaryPrestigeDTO>> getBannedUsers() {
        List<UserSummaryPrestigeDTO> bannedUsers = userService.getBannedUsers();
        return ResponseEntity.ok(bannedUsers);
    }
}
