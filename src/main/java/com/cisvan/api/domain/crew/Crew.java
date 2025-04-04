package com.cisvan.api.domain.crew;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "title_crew")
public class Crew {

    @Id
    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "directors", columnDefinition = "TEXT[]")
    private List<String> directors; // Mapeo de array a List<String>

    @Column(name = "writers", columnDefinition = "TEXT[]")
    private List<String> writers; // Mapeo de array a List<String>
}
