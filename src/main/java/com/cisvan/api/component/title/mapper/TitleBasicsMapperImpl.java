package com.cisvan.api.component.title.mapper;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

import com.cisvan.api.component.title.TitleBasics;
import com.cisvan.api.component.title.dto.TitleBasicsDTO;

@Component
public class TitleBasicsMapperImpl implements TitleBasicsMapper {

    @Override
    public TitleBasicsDTO toDTO(TitleBasics titleBasics) {
        if ( titleBasics == null ) {
            return null;
        }

        TitleBasicsDTO titleBasicsDTO = new TitleBasicsDTO();

        titleBasicsDTO.setTconst( titleBasics.getTconst() );
        titleBasicsDTO.setTitleType( titleBasics.getTitleType() );
        titleBasicsDTO.setPrimaryTitle( titleBasics.getPrimaryTitle() );
        titleBasicsDTO.setOriginalTitle( titleBasics.getOriginalTitle() );
        titleBasicsDTO.setIsAdult( titleBasics.getIsAdult() );
        titleBasicsDTO.setStartYear( titleBasics.getStartYear() );
        titleBasicsDTO.setEndYear( titleBasics.getEndYear() );
        titleBasicsDTO.setRuntimeMinutes( titleBasics.getRuntimeMinutes() );
        List<String> list = titleBasics.getGenres();
        if ( list != null ) {
            titleBasicsDTO.setGenres( new ArrayList<String>( list ) );
        }
        titleBasicsDTO.setPosterUrl( titleBasics.getPosterUrl() );

        return titleBasicsDTO;
    }
}
