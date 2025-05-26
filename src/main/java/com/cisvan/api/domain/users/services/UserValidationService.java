package com.cisvan.api.domain.users.services;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cisvan.api.common.OperationResult;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.UsersRepository;
import com.cisvan.api.helper.ValidationHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserValidationService {

    private final UsersRepository usersRepository;
    private final ValidationHelper validationHelper;

    public boolean validateUsernameAvailable(String username, OperationResult result) {
        if (usersRepository.findByUsername(username).isPresent()) {
            validationHelper.addObjectError("username", "UsernameAlreadyExists", result);
            return false;
        }
        return true;
    }

    public boolean validateEmailAvailable(String email, OperationResult result) {
        if (usersRepository.existsByEmail(email.trim().toLowerCase())) {
            validationHelper.addObjectError("email", "EmailAlreadyExists", result);
            return false;
        }
        return true;
    }

    public Optional<Users> validateUserExistsByEmail(String email, OperationResult result) {
        Optional<Users> userOpt = usersRepository.findByEmail(email.trim().toLowerCase());
    
        if (userOpt.isEmpty()) {
            validationHelper.addObjectError("email", "UserNotFound", result);
        }
    
        return userOpt;
    }

    public boolean validatePasswordMatches(Users user, String rawPassword, OperationResult result) {
        if (!new BCryptPasswordEncoder(12).matches(rawPassword, user.getPassword())) {
            validationHelper.addObjectError("password", "IncorrectPassword", result);
            return false;
        }
        return true;
    }

    public boolean validateEmailVerified(Users user, OperationResult result) {
        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            validationHelper.addObjectError("email", "EmailNotVerified", result);
            return false;
        }
        return true;
    }

    public boolean validateBanned(Users user, OperationResult result) {
        if (Boolean.TRUE.equals(user.getBanned())) {
            validationHelper.addObjectError("email", "EmailNotVerified", result);
            return false;
        }
        return true;
    }


    public boolean validateVerificationCode(Users user, String code, OperationResult result) {
        boolean expired = user.getPasswordResetExpiresAt() == null
                || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now());

        if (expired || !code.equals(user.getEmailVerificationCode())) {
            validationHelper.addObjectError("code", "InvalidOrExpiredCode", result);
            return false;
        }

        return true;
    }

    public boolean validateResetCode(Users user, String code, OperationResult result) {
        boolean expired = user.getPasswordResetExpiresAt() == null
                || user.getPasswordResetExpiresAt().isBefore(LocalDateTime.now());

        if (expired || !code.equals(user.getPasswordResetCode())) {
            validationHelper.addObjectError("code", "InvalidOrExpiredResetCode", result);
            return false;
        }

        return true;
    }

    public boolean validateImageSize(MultipartFile file, OperationResult result) {
        // 2MB = 2 * 1024 * 1024 bytes
        long maxSizeInBytes = 2 * 1024 * 1024;
        if (file != null && file.getSize() > maxSizeInBytes) {
            validationHelper.addObjectError("file", "FileTooLarge", result);
            return false;
        }
        return true;
    }
}
