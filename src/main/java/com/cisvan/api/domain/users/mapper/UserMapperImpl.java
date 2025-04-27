package com.cisvan.api.domain.users.mapper;

import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.dto.response.UserDTO;
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
}
