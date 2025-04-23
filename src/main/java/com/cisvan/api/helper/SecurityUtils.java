package com.cisvan.api.helper;


import org.springframework.security.core.context.SecurityContextHolder;

import com.cisvan.api.domain.users.UserPrincipal;
import com.cisvan.api.domain.users.Users;

public class SecurityUtils {

    public static Users getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUser();
        }
        throw new IllegalStateException("Usuario no autenticado o tipo inválido");
    }
}