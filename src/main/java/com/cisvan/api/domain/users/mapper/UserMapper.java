package com.cisvan.api.domain.users.mapper;

import org.mapstruct.Mapping;

import com.cisvan.api.domain.userfollow.dtos.FollowStatsDTO;
import com.cisvan.api.domain.userprestige.dtos.UserPrestigeDTO;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.dto.response.UserDTO;
import com.cisvan.api.domain.users.dto.response.UserProfileDTO;
import com.cisvan.api.domain.users.dto.response.UserSummaryPrestigeDTO;

public interface UserMapper {
    
    UserDTO toDTO(Users user);

    @Mapping(target = "followStats", source = "stats")
    UserProfileDTO toDto(Users user, FollowStatsDTO stats);

    // No es necesario MapStruct aquí si lo haces a mano
    default UserSummaryPrestigeDTO toSummaryPrestige(Users user, UserPrestigeDTO prestige) {
        if (user == null) return null;

        return UserSummaryPrestigeDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .profileImageUrl(user.getProfileImageUrl() != null
                ? user.getProfileImageUrl()
                : "https://cisvan.s3.us-west-1.amazonaws.com/1.jpg")
            .currentRank(prestige != null ? prestige.getCurrentRank() : 0)
            .trendDirection(prestige != null ? prestige.getTrendDirection() : null)
            .build();
    }
}
