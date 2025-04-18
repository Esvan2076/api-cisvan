package com.cisvan.api.domain.name.dto;

import lombok.Data;
import java.util.List;

@Data
public class NameAdvancedSearchDTO {

    private String name; // nombre parcial o completo
    private List<String> professions; // profesiones a filtrar
}
