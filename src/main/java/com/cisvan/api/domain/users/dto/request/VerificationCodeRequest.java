package com.cisvan.api.domain.users.dto.request;

import com.cisvan.api.validation.groups.BasicChecks;

import jakarta.validation.GroupSequence;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
@GroupSequence({VerificationCodeRequest.class, BasicChecks.class})
public class VerificationCodeRequest {

    @NotBlank(message = "{NotBlank}")
    @Email(groups = BasicChecks.class, message = "{Email}")
    private String email;
    
    @NotBlank(message = "{NotBlank}")
    private String code;
}
