package com.cisvan.api.domain.userprestige.mapper;

import com.cisvan.api.domain.userprestige.UserPrestige;
import com.cisvan.api.domain.userprestige.dtos.UserPrestigeDTO;

public interface UserPrestigeMapper {

    UserPrestigeDTO toDto(UserPrestige entity);
}