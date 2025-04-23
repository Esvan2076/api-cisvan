package com.cisvan.api.domain.users.services;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.UsersRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;

    @Transactional
    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email.trim().toLowerCase());
    }

    public Users create(Users users) {
        return usersRepository.save(users);
    }
}
