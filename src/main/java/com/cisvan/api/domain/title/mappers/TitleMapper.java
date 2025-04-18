package com.cisvan.api.domain.title.mappers;

import org.mapstruct.Mapping;

import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.TitleBasicDTO;
import com.cisvan.api.domain.title.dtos.TitleDTO;
import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;

public interface TitleMapper {

    @Mapping(target = "titleRatings", ignore = true)
    @Mapping(target = "directos", ignore = true)
    @Mapping(target = "writers", ignore = true)
    @Mapping(target = "streamingServices", ignore = true)
    TitleBasicDTO toDTO(Title title);

    // Este podés dejarlo si también necesitas convertir desde un TitleDTO
    @Mapping(target = "titleRatings", ignore = true) // antes estaba mal
    @Mapping(target = "directos", ignore = true)
    @Mapping(target = "writers", ignore = true)
    TitleBasicDTO toDTO(TitleDTO titleDTO);

    @Mapping(target = "titleRatings", ignore = true)
    TitleKnownForDTO toKnownForDTO(Title title);
}
