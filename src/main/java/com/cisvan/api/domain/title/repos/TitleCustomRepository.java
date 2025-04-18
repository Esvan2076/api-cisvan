package com.cisvan.api.domain.title.repos;

import com.cisvan.api.domain.title.Title;
import com.cisvan.api.domain.title.dtos.searchDTO.TitleAdvancedSearchDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TitleCustomRepository {
    Page<Title> advancedSearch(TitleAdvancedSearchDTO criteria, Pageable pageable);
}