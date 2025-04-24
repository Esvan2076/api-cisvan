package com.cisvan.api.validation.validators;

import com.cisvan.api.validation.annotations.ValidPassword;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) return false;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (password.length() < 8 || password.length() > 20) {
            context.buildConstraintViolationWithTemplate("{PasswordLength}")
                   .addConstraintViolation();
            valid = false;
        }

        if (!password.matches(".*[A-Z].*")) {
            context.buildConstraintViolationWithTemplate("{PasswordUppercase}")
                   .addConstraintViolation();
            valid = false;
        }

        if (!password.matches(".*[a-z].*")) {
            context.buildConstraintViolationWithTemplate("{PasswordLowercase}")
                   .addConstraintViolation();
            valid = false;
        }

        if (!password.matches(".*\\d.*")) {
            context.buildConstraintViolationWithTemplate("{PasswordDigit}")
                   .addConstraintViolation();
            valid = false;
        }

        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`].*")) {
            context.buildConstraintViolationWithTemplate("{PasswordSpecialChar}")
                   .addConstraintViolation();
            valid = false;
        }
        
        if (!valid) {
            context.buildConstraintViolationWithTemplate("{InvalidPassword}")
                   .addConstraintViolation();
        }

        return valid;
    }
}
