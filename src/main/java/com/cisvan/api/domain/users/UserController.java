package com.cisvan.api.domain.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.users.dto.request.AuthRequest;
import com.cisvan.api.domain.users.dto.request.EmailRequest;
import com.cisvan.api.domain.users.dto.request.LoginRequest;
import com.cisvan.api.domain.users.dto.request.ResetPasswordRequest;
import com.cisvan.api.domain.users.dto.request.VerificationCodeRequest;
import com.cisvan.api.domain.users.dto.response.EmailVerificationResponse;
import com.cisvan.api.helper.ControllerHelper;
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final ControllerHelper controllerHelper;
    private final UserOrchestrator userOrchestrator;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request, BindingResult result) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);

        Optional<EmailVerificationResponse> response = userOrchestrator.register(request, operationResult);
        if (operationResult.hasErrors()) return ResponseEntity.unprocessableEntity().body(operationResult);

        return ResponseEntity.status(HttpStatus.CREATED).body(response.get());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult result) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);
    
        Optional<String> jwtOpt = userOrchestrator.login(request, operationResult);
        if (operationResult.hasErrors()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(operationResult);
    
        return ResponseEntity.ok(jwtOpt.get());
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpServletRequest request) {
        return userOrchestrator.getMe(request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
        @Valid @RequestBody EmailRequest request,
        BindingResult result
    ) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);
    
        userOrchestrator.forgotPassword(request.getEmail(), operationResult);
    
        return ResponseEntity.ok().build();
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
}
