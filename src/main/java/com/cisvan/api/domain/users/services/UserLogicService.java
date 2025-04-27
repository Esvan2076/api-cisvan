package com.cisvan.api.domain.users.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.UsersRepository;
import com.cisvan.api.services.JwtService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserLogicService {
    
    private final UsersRepository usersRepository;
    private final JwtService jwtService;

    public Optional<Users> getUserFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Optional.empty();
        }

        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);

        return usersRepository.findById(userId);
    }
}
