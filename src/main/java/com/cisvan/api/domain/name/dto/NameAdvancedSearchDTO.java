package com.cisvan.api.domain.name.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NameAdvancedSearchDTO {
    
    private String name; // nombre parcial o completo
    private List<String> professions; // profesiones a filtrar
}