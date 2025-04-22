package com.cisvan.api.domain.users;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.users.dto.AuthRequest;
import com.cisvan.api.domain.users.dto.EmailVerificationResponse;
import com.cisvan.api.domain.users.dto.LoginRequest;
import com.cisvan.api.services.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final JwtService jwtService;
    private final Random random = new Random();

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public Optional<EmailVerificationResponse> register(AuthRequest request, OperationResult operationResult) {
        if (usersRepository.findByUsername(request.getUsername()).isPresent()) {
            operationResult.addError("username", "Username ya existe");
            return Optional.empty();
        }

        if (usersRepository.existsByEmail(request.getEmail())) {
            operationResult.addError("email", "Email ya existe");
            return Optional.empty();
        }

        // Generar código PIN de 4 dígitos
        String code = String.format("%04d", random.nextInt(10000));

        Users user = Users.builder()
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .email(request.getEmail())
                .admin(false)
                .emailVerified(false)
                .emailVerificationCode(code)
                .passwordResetExpiresAt(LocalDateTime.now().plusMinutes(10)) // Expiración para validación del código
                .build();

        usersRepository.save(user);

        return Optional.of(new EmailVerificationResponse(
            user.getUsername(),
            user.getEmail(),
            code
        ));
    }

    public String verifyLogin(LoginRequest request) {
        Optional<Users> userOpt = usersRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return "fail";
        }

        Users user = userOpt.get();

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            return "fail";
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            return "unverified";
        }

        return jwtService.generateToken(user.getId(), user.getUsername(), user.getAdmin());
    }
}
