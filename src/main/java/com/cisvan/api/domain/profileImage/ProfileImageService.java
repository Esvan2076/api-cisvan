package com.cisvan.api.domain.profileImage;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final ProfileImageRepository profileImageRepository;

    public List<ProfileImage> getProfileImagesByUserId(Long userId) {
        return profileImageRepository.findByUserId(userId);
    }

    public ProfileImage create(ProfileImage profileImage) {
        return profileImageRepository.save(profileImage);
    }
}
