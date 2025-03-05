package com.cisvan.api.component.akas;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Data
@Embeddable
public class TitleAkasId implements Serializable {

    @Column(name = "titleid")
    private String titleId;

    @Column(name = "ordering")
    private Integer ordering;
}
