package com.cisvan.api.domain.name.repos;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cisvan.api.domain.name.Name;
import com.cisvan.api.domain.name.dto.NameAdvancedSearchDTO;

public interface NameCustomRepository {
    Page<Name> advancedSearch(NameAdvancedSearchDTO criteria, Pageable pageable);
}