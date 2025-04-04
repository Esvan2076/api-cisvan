package com.cisvan.api.domain.principal;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class PrincipalId implements Serializable {

    @Column(name = "tconst", length = 15)
    private String tconst;
    
    @Column(name = "ordering")
    private Short ordering;
}
