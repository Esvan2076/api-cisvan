package com.cisvan.api.domain.akas;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AkasId implements Serializable {

    @Column(name = "tconst", length = 15, nullable = false)
    private String tconst;

    @Column(name = "ordering", nullable = false)
    private Short ordering;
}