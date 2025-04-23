package com.cisvan.api.domain.principal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "title_principals")
@Access(AccessType.FIELD)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Principal {

    @EmbeddedId
    private PrincipalId id;

    @Column(name = "nconst", length = 15)
    private String nconst;

    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @Column(name = "job", length = 200)
    private String job;

    @Column(name = "characters", length = 200)
    private String characters;
}