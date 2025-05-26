package com.cisvan.api.domain.users.services;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cisvan.api.domain.userprestige.UserPrestigeService;
import com.cisvan.api.domain.userprestige.dtos.UserPrestigeDTO;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.UsersRepository;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;
import com.cisvan.api.domain.users.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final UserPrestigeService prestigeService;
    private final UserMapper userMapper;

    @Transactional
    public Optional<Users> findByEmail(String email) {
        return usersRepository.findByEmail(email.trim().toLowerCase());
    }

    public Users create(Users users) {
        return usersRepository.save(users);
    }

    public Optional<Users> getById(Long id) {
        return usersRepository.findById(id);
    }

    public Page<Users> searchByUsername(String username, Pageable pageable) {
        return usersRepository.findByUsernameContainingIgnoreCase(username, pageable);
    }

    @Transactional
    public boolean toggleBanUser(Long userId) {
        Users user = getById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setBanned(!Boolean.TRUE.equals(user.getBanned()));
        boolean ban = user.getBanned();
        if(ban){
            user.setProfileImageUrl("https://n987nxe95xch9b4.s3.us-east-1.amazonaws.com/imagenes/perfil/Ban.png");
        }
        usersRepository.save(user); // asegúrate de tener este método
        return ban;
    }

    public List<UserSummaryPrestigeDTO> getBannedUsers() {
        return getAllBannedUsers().stream()
            .map(user -> {
                UserPrestigeDTO prestige = prestigeService.getPrestigeDTOByUserId(user.getId()).orElse(null);
                return userMapper.toSummaryPrestige(user, prestige);
            })
            .toList();
    }

    public List<Users> getAllBannedUsers() {
        return usersRepository.findAllByBannedTrue();
    }
}
