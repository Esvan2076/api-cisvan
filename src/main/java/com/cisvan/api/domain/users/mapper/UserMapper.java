package com.cisvan.api.domain.users.mapper;

import org.mapstruct.Mapping;

import com.cisvan.api.domain.userfollow.dtos.FollowStatsDTO;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.dto.response.UserDTO;
import com.cisvan.api.domain.users.dto.response.UserProfileDTO;

public interface UserMapper {
    
    UserDTO toDTO(Users user);

    @Mapping(target = "followStats", source = "stats")
    UserProfileDTO toDto(Users user, FollowStatsDTO stats);
}
