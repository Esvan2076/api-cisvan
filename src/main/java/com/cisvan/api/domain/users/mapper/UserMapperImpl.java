package com.cisvan.api.domain.users.mapper;

import com.cisvan.api.domain.userfollow.dtos.FollowStatsDTO;
import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.dto.response.UserDTO;
import com.cisvan.api.domain.users.dto.response.UserProfileDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDTO toDTO(Users user) {
        if ( user == null ) {
            return null;
        }

        UserDTO.UserDTOBuilder userDTO = UserDTO.builder();

        userDTO.id( user.getId() );
        userDTO.username( user.getUsername() );
        userDTO.admin( user.getAdmin() );

        return userDTO.build();
    }

    @Override
    public UserProfileDTO toDto(Users user, FollowStatsDTO stats) {
        if ( user == null && stats == null ) {
            return null;
        }

        UserProfileDTO.UserProfileDTOBuilder userProfileDTO = UserProfileDTO.builder();

        if ( user != null ) {
            userProfileDTO.id( user.getId() );
            userProfileDTO.username( user.getUsername() );
            userProfileDTO.profileImageUrl( user.getProfileImageUrl() );
        }
        userProfileDTO.followStats( stats );

        return userProfileDTO.build();
    }
}
