package com.cisvan.api.domain.users.mapper;

import com.cisvan.api.domain.users.Users;
import com.cisvan.api.domain.users.dto.response.UserDTO;

public interface UserMapper {
    
    UserDTO toDTO(Users user);
}
