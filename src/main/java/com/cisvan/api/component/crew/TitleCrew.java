package com.cisvan.api.component.crew;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "title_crew")
public class TitleCrew {

    @Id
    @Column(name = "tconst", nullable = false)
    private String tconst;

    @Column(name = "directors")
    private String directors;

    @Column(name = "writers")
    private String writers;
}
