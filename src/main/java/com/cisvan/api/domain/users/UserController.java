package com.cisvan.api.domain.users;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.users.dto.*;
import com.cisvan.api.helper.ControllerHelper;
import com.cisvan.api.services.JwtService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;
    private final ControllerHelper controllerHelper;
    private final UsersRepository usersRepository;
    private final BCryptPasswordEncoder encoder;

    private String generateCode() {
        return String.valueOf(new Random().nextInt(9000) + 1000); // 4 dígitos
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request, BindingResult result) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);

        Optional<EmailVerificationResponse> response = userService.register(request, operationResult);
        if (operationResult.hasErrors()) return ResponseEntity.unprocessableEntity().body(operationResult);

        return ResponseEntity.status(HttpStatus.CREATED).body(response.get());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, BindingResult result) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);

        String token = userService.verifyLogin(request);

        if ("unverified".equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Please verify your email before logging in.");
        }

        if ("fail".equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
        }

        return ResponseEntity.ok(token);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getMe(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long userId = jwtService.extractUserId(token);
            String username = jwtService.extractUserName(token);
            Boolean admin = jwtService.extractAdmin(token);

            return ResponseEntity.ok(new UserDTO(userId, username, admin));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmailByCode(
        @Valid @RequestBody EmailVerificationRequest request,
        BindingResult result
    ) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Invalid data");
        }
    
        Optional<Users> userOpt = usersRepository.findByEmail(request.getEmail());
    
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid email or code.");
        }
    
        Users user = userOpt.get();
    
        if (user.getPasswordResetExpiresAt() == null ||
            user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Verification code expired.");
        }
    
        if (!request.getCode().equals(user.getEmailVerificationCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid verification code.");
        }
    
        // ✅ Verifica el correo
        user.setEmailVerified(true);
        user.setEmailVerificationCode(null);
        user.setPasswordResetExpiresAt(null);
        usersRepository.save(user);
    
        // ✅ Genera el JWT
        String jwt = jwtService.generateToken(user.getId(), user.getUsername(), user.getAdmin());
    
        return ResponseEntity.ok(jwt);
    }    

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendEmailVerificationCode(
        @Valid @RequestBody ResendVerificationRequest request,
        BindingResult result
    ) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);

        Optional<Users> userOpt = usersRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.ok("If the email exists, a verification code will be sent.");
        }

        Users user = userOpt.get();

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Email is already verified.");
        }

        // Generar nuevo código de 4 dígitos
        String newCode = String.format("%04d", (int)(Math.random() * 10000));

        user.setEmailVerificationCode(newCode);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(10));
        usersRepository.save(user);

        return ResponseEntity.ok(new EmailVerificationResponse(
            user.getUsername(),
            user.getEmail(),
            newCode
        ));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request,
        BindingResult result
    ) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);

        Optional<Users> userOpt = usersRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.ok("If the email exists, a reset code will be sent.");
        }

        Users user = userOpt.get();

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email not verified.");
        }

        String code = generateCode();
        user.setPasswordResetCode(code);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(10));
        usersRepository.save(user);

        return ResponseEntity.ok(new PasswordResetEmailDTO(
            user.getEmail(),
            user.getUsername(),
            code
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request,
        BindingResult result
    ) {
        OperationResult operationResult = controllerHelper.validate(result);
        if (operationResult.hasErrors()) return ResponseEntity.badRequest().body(operationResult);

        Optional<Users> userOpt = usersRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request.");
        }

        Users user = userOpt.get();

        if (user.getPasswordResetExpiresAt() == null || 
            user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now()) || 
            !request.getCode().equals(user.getPasswordResetCode())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired reset code.");
        }

        user.setPassword(encoder.encode(request.getNewPassword()));
        user.setPasswordResetCode(null);
        user.setPasswordResetExpiresAt(null);
        usersRepository.save(user);

        return ResponseEntity.ok("Password has been successfully updated.");
    }
}
