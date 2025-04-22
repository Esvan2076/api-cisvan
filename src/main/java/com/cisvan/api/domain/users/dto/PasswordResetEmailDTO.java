package com.cisvan.api.domain.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResetEmailDTO {
    private String email;
    private String username;
    private String token;
}