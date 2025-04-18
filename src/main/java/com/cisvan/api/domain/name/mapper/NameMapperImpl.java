package com.cisvan.api.domain.name.mapper;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.dto.NameAdvancedSearchResultDTO;
import com.cisvan.api.domain.name.dto.NameBasicDTO;
import com.cisvan.api.domain.name.dto.NameEssencialDTO;
import com.cisvan.api.domain.name.dto.NameSearchResultDTO;
import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.PrincipalTitleDTO;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class NameMapperImpl implements NameMapper {

    @Override
    public NameEssencialDTO toDTO(Name name) {
        if ( name == null ) {
            return null;
        }

        NameEssencialDTO nameEssencialDTO = new NameEssencialDTO();

        nameEssencialDTO.setNconst( name.getNconst() );
        nameEssencialDTO.setPrimaryName( name.getPrimaryName() );

        return nameEssencialDTO;
    }

    @Override
    public NameBasicDTO toBasicDTO(Name name) {
        if ( name == null ) {
            return null;
        }

        NameBasicDTO nameBasicDTO = new NameBasicDTO();

        nameBasicDTO.setNconst( name.getNconst() );
        nameBasicDTO.setPrimaryName( name.getPrimaryName() );
        nameBasicDTO.setBirthYear( name.getBirthYear() );
        nameBasicDTO.setDeathYear( name.getDeathYear() );
        List<String> list = name.getPrimaryProfession();
        if ( list != null ) {
            nameBasicDTO.setPrimaryProfession( new ArrayList<String>( list ) );
        }
        List<String> list1 = name.getKnownForTitles();
        if ( list1 != null ) {
            nameBasicDTO.setKnownForTitles( new ArrayList<String>( list1 ) );
        }
        nameBasicDTO.setImageUrl( name.getImageUrl() );

        return nameBasicDTO;
    }

    @Override
    public NameSearchResultDTO toSearchResultDTO(Name name) {
        if ( name == null ) {
            return null;
        }

        NameSearchResultDTO nameSearchResultDTO = new NameSearchResultDTO();

        nameSearchResultDTO.setNconst( name.getNconst() );
        nameSearchResultDTO.setPrimaryName( name.getPrimaryName() );

        return nameSearchResultDTO;
    }

    @Override
    public PrincipalTitleDTO toPrincipalTitleDTO(Title title) {
        if ( title == null ) {
            return null;
        }

        PrincipalTitleDTO principalTitleDTO = new PrincipalTitleDTO();

        principalTitleDTO.setPrimaryTitle( title.getPrimaryTitle() );
        principalTitleDTO.setStartYear( title.getStartYear() );
        principalTitleDTO.setEndYear( title.getEndYear() );

        return principalTitleDTO;
    }

    @Override
    public NameAdvancedSearchResultDTO toAdvancedSearchResultDTO(Name name) {
        if ( name == null ) {
            return null;
        }

        NameAdvancedSearchResultDTO nameAdvancedSearchResultDTO = new NameAdvancedSearchResultDTO();

        nameAdvancedSearchResultDTO.setNconst( name.getNconst() );
        nameAdvancedSearchResultDTO.setPrimaryName( name.getPrimaryName() );
        nameAdvancedSearchResultDTO.setBirthYear( name.getBirthYear() );
        List<String> list = name.getPrimaryProfession();
        if ( list != null ) {
            nameAdvancedSearchResultDTO.setPrimaryProfession( new ArrayList<String>( list ) );
        }
        nameAdvancedSearchResultDTO.setImageUrl( name.getImageUrl() );

        return nameAdvancedSearchResultDTO;
    }
}
