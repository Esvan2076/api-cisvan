package com.cisvan.api.domain.name.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.dto.NameBasicDTO;
import com.cisvan.api.domain.name.dto.NameSearchResultDTO;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.PrincipalTitleDTO;

@Mapper(componentModel = "spring")
public interface NameMapper {
    NameBasicDTO toDTO(Name name);

    @Mapping(target = "primaryProfession", ignore = true)
    @Mapping(target = "principalTitle", ignore = true)
    NameSearchResultDTO toSearchResultDTO(Name name);

    PrincipalTitleDTO toPrincipalTitleDTO(Title title);
}
