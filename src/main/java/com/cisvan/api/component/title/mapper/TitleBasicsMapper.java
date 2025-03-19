package com.cisvan.api.component.title.mapper;

import org.mapstruct.Mapping;

import com.cisvan.api.component.title.TitleBasics;
import com.cisvan.api.component.title.dto.TitleBasicsDTO;

public interface TitleBasicsMapper {
   
    @Mapping(target = "ratings", ignore = true) // Se asignará manualmente
    @Mapping(target = "directos", ignore = true) // Se asignará manualmente
    @Mapping(target = "writers", ignore = true) // Se asignará manualmente
    TitleBasicsDTO toDTO(TitleBasics titleBasics);
}
