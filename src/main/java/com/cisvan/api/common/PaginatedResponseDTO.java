package com.cisvan.api.common;

import lombok.Data;
import java.util.List;

@Data
public class PaginatedResponseDTO<T> {
    
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}