package com.cisvan.api.domain.name;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.cisvan.api.domain.name.dto.NameAdvancedSearchDTO;
import com.cisvan.api.domain.name.dto.NameAdvancedSearchResultDTO;
import com.cisvan.api.domain.name.dto.NameBasicDTO;
import com.cisvan.api.domain.name.mapper.NameMapper;
import com.cisvan.api.domain.name.services.NameService;
import com.cisvan.api.domain.namerating.services.NameRatingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NameOrchestrator {
    
    private final NameMapper nameMapper;
    private final NameService nameService;
    private final NameRatingService nameRatingService;
    
    public Optional<NameBasicDTO> getNameBasicById(String nconst) {
        Optional<Name> nameOpt = nameService.findById(nconst);
        if (nameOpt.isEmpty()) {
            return Optional.empty();
        }
    
        Name name = nameOpt.get();
        NameBasicDTO dto = nameMapper.toBasicDTO(name);
    
        // Obtener y asociar rating si existe
        nameRatingService.getNameRatingById(nconst).ifPresent(dto::setNameRatings);
    
        return Optional.of(dto);
    }

    public Page<NameAdvancedSearchResultDTO> searchAdvancedNames(NameAdvancedSearchDTO criteria, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        Page<Name> namesPage = nameService.advancedSearch(criteria, pageable);

        List<NameAdvancedSearchResultDTO> content = namesPage.getContent().stream()
            .map(nameMapper::toAdvancedSearchResultDTO)
            .toList();

        return new PageImpl<>(content, pageable, namesPage.getTotalElements());
    }
}
