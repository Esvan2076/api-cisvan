package com.cisvan.api.domain.title.dtos.searchDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SerieSearchResultDTO extends ContentSearchResultDTO {

    private Short endYear;
}