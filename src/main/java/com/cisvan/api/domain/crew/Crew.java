package com.cisvan.api.domain.crew;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "title_crew")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Crew {

    @Id
    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Builder.Default
    @Column(name = "directors", columnDefinition = "TEXT[]", nullable = true)
    private List<String> directors = new ArrayList<>();

    @Builder.Default
    @Column(name = "writers", columnDefinition = "TEXT[]", nullable = true)
    private List<String> writers = new ArrayList<>();

    public List<String> getDirectors() {
        // Opción B: Asegurar que no se devuelva null en el getter
        return directors == null ? Collections.emptyList() : directors;
    }
}
