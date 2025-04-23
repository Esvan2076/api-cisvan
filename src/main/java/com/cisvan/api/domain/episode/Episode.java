package com.cisvan.api.domain.episode;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "title_episode")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Episode {

    @Id
    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "parent_tconst", length = 15, nullable = false)
    private String parentTconst;

    @Column(name = "season_number")
    private Short seasonNumber;

    @Column(name = "episode_number")
    private Short episodeNumber;
}