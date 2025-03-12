package com.cisvan.api.component.episode;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "title_episode")
public class TitleEpisode {

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
