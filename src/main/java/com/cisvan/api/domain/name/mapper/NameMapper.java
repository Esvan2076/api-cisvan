package com.cisvan.api.domain.name.mapper;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.dto.NameBasicDTO;

public interface NameMapper {
    NameBasicDTO toDTO(Name name);
}
