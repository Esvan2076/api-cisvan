package com.cisvan.api.component.name.mapper;

import org.springframework.stereotype.Component;

import com.cisvan.api.component.name.NameBasics;
import com.cisvan.api.component.name.dto.NameBasicsDTO;

@Component
public class NameBasicsMapperImpl implements NameBasicsMapper {

    @Override
    public NameBasicsDTO toDTO(NameBasics nameBasics) {
        if ( nameBasics == null ) {
            return null;
        }

        NameBasicsDTO nameBasicsDTO = new NameBasicsDTO();

        nameBasicsDTO.setNconst( nameBasics.getNconst() );
        nameBasicsDTO.setPrimaryName( nameBasics.getPrimaryName() );

        return nameBasicsDTO;
    }
}
