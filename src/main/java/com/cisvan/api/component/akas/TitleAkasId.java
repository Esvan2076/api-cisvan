package com.cisvan.api.component.akas;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class TitleAkasId implements Serializable {

    @Column(name = "tconst", length = 15)
    private String tconst;

    @Column(name = "ordering")
    private Short ordering;
}
