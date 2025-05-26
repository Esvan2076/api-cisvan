package com.cisvan.api.domain.title.dtos.searchDTO;

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
public class TitleAdvancedSearchDTO {

    // Nombre parcial o completo del título
    private String name;

    // Lista de tipos (ej. movie, short, tvSeries)
    private List<String> types;

    // Lista de géneros (ej. Action, Drama, Comedy)
    private List<String> genres;

    // Lista de plataformas de streaming (ej. Netflix, Prime Video, etc.)
    private String streamingServices;

    // NUEVO: categoría predefinida (1=valoradas, 2=populares, 3=recientes)
    private Short category;
}
