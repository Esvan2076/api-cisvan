package com.cisvan.api.domain.users.mapper;

import org.mapstruct.Mapper;

import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.dto.response.UserDTO;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserDTO toDTO(Users user);
}
