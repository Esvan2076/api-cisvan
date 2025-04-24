package com.cisvan.api.domain.users.dto.request;

import com.cisvan.api.validation.annotations.ValidPassword;
import com.cisvan.api.validation.groups.BasicChecks;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@GroupSequence({AuthRequest.class, BasicChecks.class})
public class AuthRequest {

    @NotBlank(message = "{NotBlank}")
    @Size(groups = BasicChecks.class, min = 5, max = 50, message = "{SizeRange}")
    private String username;

    @NotBlank(message = "{NotBlank}")
    @ValidPassword
    private String password;

    @NotBlank(message = "{NotBlank}")
    @Email(groups = BasicChecks.class, message = "{Email}")
    private String email;
}
