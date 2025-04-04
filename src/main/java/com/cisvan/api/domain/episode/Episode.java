package com.cisvan.api.domain.episode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "title_episode")
public class Episode {

    @Id
    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst; // Clave primaria

    @Column(name = "parent_tconst", length = 15, nullable = false)
    private String parentTconst;

    @Column(name = "season_number")
    private Short seasonNumber; // `SMALLINT` en PostgreSQL → `Short` en Java

    @Column(name = "episode_number")
    private Short episodeNumber; // `SMALLINT` en PostgreSQL → `Short` en Java
}
