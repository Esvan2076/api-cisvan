package com.cisvan.api.component.name.mapper;

import com.cisvan.api.component.name.NameBasics;
import com.cisvan.api.component.name.dto.NameBasicsDTO;

public interface NameBasicsMapper {
    NameBasicsDTO toDTO(NameBasics nameBasics);
}
