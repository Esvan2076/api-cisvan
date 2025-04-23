package com.cisvan.api.domain.users;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.users.dto.request.AuthRequest;
import com.cisvan.api.domain.users.dto.request.LoginRequest;
import com.cisvan.api.domain.users.dto.request.ResetPasswordRequest;
import com.cisvan.api.domain.users.dto.request.VerificationCodeRequest;
import com.cisvan.api.domain.users.dto.response.EmailVerificationResponse;
import com.cisvan.api.domain.users.dto.response.UserDTO;
import com.cisvan.api.domain.users.services.UserService;
import com.cisvan.api.domain.users.services.UserValidationService;
import com.cisvan.api.services.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserOrchestrator {

    private final UserValidationService userValidationService;
    private final UserService userService;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder;
    private final Random random = new Random();
    
    @Transactional
    public Optional<EmailVerificationResponse> register(AuthRequest request, OperationResult operationResult) {
        // Validar si el username ya existe
        if (!userValidationService.validateUsernameAvailable(request.getUsername(), operationResult)) {
            return Optional.empty();
        }

        // Normalizar email
        String email = request.getEmail().trim().toLowerCase();

        // Validar si el email ya está registrado
        if (!userValidationService.validateEmailAvailable(request.getEmail(), operationResult)) {
            return Optional.empty();
        }

        // Generar código PIN de 4 dígitos
        String code = generate4DigitCode();

        // Crear usuario nuevo
        Users user = Users.builder()
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .email(email)
                .admin(false)
                .emailVerified(false)
                .emailVerificationCode(code)
                .passwordResetExpiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        // Guardar en base de datos
        userService.create(user);

        // Devolver respuesta con el código de verificación
        return Optional.of(new EmailVerificationResponse(
                user.getUsername(),
                user.getEmail(),
                code
        ));
    }

    public String generate4DigitCode() {
        return String.format("%04d", random.nextInt(10000));
    }

    @Transactional(readOnly = true)
    public Optional<String> login(LoginRequest request, OperationResult result) {
        String email = request.getEmail().trim().toLowerCase();

        Optional<Users> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        Users user = userOpt.get();

        if (!userValidationService.validatePasswordMatches(user, request.getPassword(), result)) {
            return Optional.empty();
        }

        if (!userValidationService.validateEmailVerified(user, result)) {
            return Optional.empty();
        }

        // Todo OK → Generar y devolver JWT
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getAdmin());
        return Optional.of(token);
    }

    public Optional<UserDTO> getMe(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUserName(token);
        Boolean admin = jwtService.extractAdmin(token);

        return Optional.of(new UserDTO(userId, username, admin));
    }

    @Transactional
    public Optional<String> verifyEmail(VerificationCodeRequest request, OperationResult result) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode();

        Optional<Users> userOpt = userValidationService.validateUserExistsByEmail(email, result);
        if (userOpt.isEmpty()) return Optional.empty();
    
        Users user = userOpt.get();

        if (!userValidationService.validateVerificationCode(user, code, result)) {
            return Optional.empty();
        }

        user.setEmailVerified(true);
        user.setEmailVerificationCode(null);
        user.setPasswordResetExpiresAt(null);
        userService.create(user);

        String jwt = jwtService.generateToken(user.getId(), user.getUsername(), user.getAdmin());
        return Optional.of(jwt);
    }

    @Transactional
    public Optional<EmailVerificationResponse> resendEmailVerificationCode(String email, OperationResult result) {
        Optional<Users> userOpt = userValidationService.validateUserExistsByEmail(email, result);
        if (userOpt.isEmpty()) return Optional.empty();
    
        Users user = userOpt.get();

        if (!userValidationService.validateEmailVerified(user, result)) {
            return Optional.empty();
        }
    
        String code = generate4DigitCode();
        user.setEmailVerificationCode(code);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(10));
        userService.create(user);
    
        return Optional.of(new EmailVerificationResponse(user.getUsername(), user.getEmail(), code));
    }    

    @Transactional
    public void forgotPassword(String email, OperationResult result) {
        Optional<Users> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) return;

        Users user = userOpt.get();

        if (!userValidationService.validateEmailVerified(user, result)) {
            return;
        }

        String code = generate4DigitCode();
        user.setPasswordResetCode(code);
        user.setPasswordResetExpiresAt(LocalDateTime.now().plusMinutes(10));
        userService.create(user);
    }

    @Transactional
    public boolean resetPassword(ResetPasswordRequest request, OperationResult result) {
        String email = request.getEmail().trim().toLowerCase();
        String code = request.getCode();
        String newPassword = request.getNewPassword();

        Optional<Users> userOpt = userValidationService.validateUserExistsByEmail(email, result);
        if (userOpt.isEmpty()) return false;
    
        Users user = userOpt.get();

        if (!userValidationService.validateResetCode(user, code, result)) {
            return false;
        }

        user.setPassword(encoder.encode(newPassword));
        user.setPasswordResetCode(null);
        user.setPasswordResetExpiresAt(null);
        userService.create(user);

        return true;
    }
}
