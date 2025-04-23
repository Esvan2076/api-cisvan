package com.cisvan.api.domain.principal;

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
public class PrincipalId implements Serializable {

    @Column(name = "tconst", length = 15)
    private String tconst;

    @Column(name = "ordering")
    private Short ordering;
}