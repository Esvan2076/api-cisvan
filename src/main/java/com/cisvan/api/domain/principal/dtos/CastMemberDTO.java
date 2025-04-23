package com.cisvan.api.domain.principal.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CastMemberDTO {

    private String nconst;
    private String primaryName;
    private String characters;
    private String imageUrl;
}