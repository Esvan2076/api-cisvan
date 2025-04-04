package com.cisvan.api.domain.title.dtos;

import java.util.List;

import com.cisvan.api.domain.name.dto.NameBasicDTO;
import com.cisvan.api.domain.rating.Rating;
import com.cisvan.api.domain.streaming.Streaming;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TitleBasicDTO extends TitleDTO {

    private Rating ratings;
    private List<NameBasicDTO> directos;
    private List<NameBasicDTO> writers;
    private List<Streaming> streamingServices;
}
