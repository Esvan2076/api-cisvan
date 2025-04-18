package com.cisvan.api.domain.title.dtos.searchDTO;

import lombok.Data;

import java.util.List;

@Data
public class TitleAdvancedSearchDTO {

    // Nombre parcial o completo del título
    private String name;

    // Lista de tipos (ej. movie, short, tvSeries)
    private List<String> types;

    // Lista de géneros (ej. Action, Drama, Comedy)
    private List<String> genres;

    // Lista de plataformas de streaming (ej. Netflix, Prime Video, etc.)
    private String streamingServices;
}
