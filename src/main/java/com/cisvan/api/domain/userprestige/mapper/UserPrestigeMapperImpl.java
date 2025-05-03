package com.cisvan.api.domain.userprestige.mapper;

import com.cisvan.api.domain.userprestige.UserPrestige;
import com.cisvan.api.domain.userprestige.dtos.UserPrestigeDTO;
import org.springframework.stereotype.Component;

@Component
public class UserPrestigeMapperImpl implements UserPrestigeMapper {

    @Override
    public UserPrestigeDTO toDto(UserPrestige entity) {
        if ( entity == null ) {
            return null;
        }

        UserPrestigeDTO userPrestigeDTO = new UserPrestigeDTO();

        userPrestigeDTO.setCurrentRank( entity.getCurrentRank() );
        userPrestigeDTO.setWeightedScore( entity.getWeightedScore() );
        userPrestigeDTO.setTrendDirection( entity.getTrendDirection() );

        return userPrestigeDTO;
    }
}
