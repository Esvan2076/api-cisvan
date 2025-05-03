package com.cisvan.api.domain.userprestige;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.userprestige.dtos.UserPrestigeDTO;
import com.cisvan.api.domain.userprestige.mapper.UserPrestigeMapper;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPrestigeService {

    private final UserPrestigeRepository userPrestigeRepository;
    private final UserPrestigeMapper userPrestigeMapper;

    public Optional<UserPrestige> getPrestigeByUserId(Long userId) {
        return userPrestigeRepository.findById(userId);
    }

    public Optional<UserPrestigeDTO> getPrestigeDTOByUserId(Long userId) {
        return userPrestigeRepository.findById(userId)
                .map(userPrestigeMapper::toDto);
    }
}