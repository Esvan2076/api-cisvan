package com.cisvan.api.domain.profileImage;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.services.UserLogicService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfileImageOrchestrator {
    
    private final ProfileImageService profileImageService;
    private final UserLogicService userLogicService;

    public List<ProfileImage> getProfileImagesByUser(HttpServletRequest request) {

        Optional<Users> userOpt = userLogicService.getUserFromRequest(request);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        Users user = userOpt.get();

        return profileImageService.getProfileImagesByUserId(user.getId());
    }
}
