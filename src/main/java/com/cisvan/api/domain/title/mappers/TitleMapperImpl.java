package com.cisvan.api.domain.title.mappers;

import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.TitleBasicDTO;
import com.cisvan.api.domain.title.dtos.TitleDTO;
import com.cisvan.api.domain.title.dtos.TitleKnownForDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TitleMapperImpl implements TitleMapper {

    @Override
    public TitleBasicDTO toDTO(Title title) {
        if ( title == null ) {
            return null;
        }

        TitleBasicDTO titleBasicDTO = new TitleBasicDTO();

        titleBasicDTO.setTconst( title.getTconst() );
        titleBasicDTO.setTitleType( title.getTitleType() );
        titleBasicDTO.setPrimaryTitle( title.getPrimaryTitle() );
        titleBasicDTO.setOriginalTitle( title.getOriginalTitle() );
        titleBasicDTO.setIsAdult( title.getIsAdult() );
        titleBasicDTO.setStartYear( title.getStartYear() );
        titleBasicDTO.setEndYear( title.getEndYear() );
        titleBasicDTO.setRuntimeMinutes( title.getRuntimeMinutes() );
        List<String> list = title.getGenres();
        if ( list != null ) {
            titleBasicDTO.setGenres( new ArrayList<String>( list ) );
        }
        titleBasicDTO.setPosterUrl( title.getPosterUrl() );

        return titleBasicDTO;
    }

    @Override
    public TitleBasicDTO toDTO(TitleDTO titleDTO) {
        if ( titleDTO == null ) {
            return null;
        }

        TitleBasicDTO titleBasicDTO = new TitleBasicDTO();

        titleBasicDTO.setTconst( titleDTO.getTconst() );
        titleBasicDTO.setTitleType( titleDTO.getTitleType() );
        titleBasicDTO.setPrimaryTitle( titleDTO.getPrimaryTitle() );
        titleBasicDTO.setOriginalTitle( titleDTO.getOriginalTitle() );
        titleBasicDTO.setIsAdult( titleDTO.getIsAdult() );
        titleBasicDTO.setStartYear( titleDTO.getStartYear() );
        titleBasicDTO.setEndYear( titleDTO.getEndYear() );
        titleBasicDTO.setRuntimeMinutes( titleDTO.getRuntimeMinutes() );
        List<String> list = titleDTO.getGenres();
        if ( list != null ) {
            titleBasicDTO.setGenres( new ArrayList<String>( list ) );
        }
        titleBasicDTO.setPosterUrl( titleDTO.getPosterUrl() );

        return titleBasicDTO;
    }

    @Override
    public TitleKnownForDTO toKnownForDTO(Title title) {
        if ( title == null ) {
            return null;
        }

        TitleKnownForDTO titleKnownForDTO = new TitleKnownForDTO();

        titleKnownForDTO.setTconst( title.getTconst() );
        titleKnownForDTO.setTitleType( title.getTitleType() );
        titleKnownForDTO.setPrimaryTitle( title.getPrimaryTitle() );
        titleKnownForDTO.setStartYear( title.getStartYear() );
        titleKnownForDTO.setPosterUrl( title.getPosterUrl() );

        return titleKnownForDTO;
    }
}
