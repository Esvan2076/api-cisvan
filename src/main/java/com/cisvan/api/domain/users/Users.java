package com.cisvan.api.domain.users;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 60)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "email_verification_code", length = 10)
    private String emailVerificationCode;

    @Column(name = "password_reset_code", length = 10)
    private String passwordResetCode;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    private Boolean admin;
}
