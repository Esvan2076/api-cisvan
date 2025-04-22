package com.cisvan.api.domain.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmailVerificationResponse {
    
    private String username;
    private String email;
    private String code; // antes: token
}