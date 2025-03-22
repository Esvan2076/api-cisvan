package com.cisvan.api.component.title.dto;

import java.util.List;

import com.cisvan.api.component.name.dto.NameBasicsDTO;
import com.cisvan.api.component.ratings.TitleRatings;
import com.cisvan.api.component.streaming.Streaming;
import com.cisvan.api.component.title.TitleBasics;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TitleBasicsDTO extends TitleBasics {
    private TitleRatings ratings;

    private List<NameBasicsDTO> directos;

    private List<NameBasicsDTO> writers;

    private List<Streaming> streamingServices;
}
