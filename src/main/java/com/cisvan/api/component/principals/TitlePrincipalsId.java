package com.cisvan.api.component.principals;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class TitlePrincipalsId implements Serializable {

    @Column(name = "tconst", length = 15)
    private String tconst;
    
    @Column(name = "ordering")
    private Short ordering;
}
