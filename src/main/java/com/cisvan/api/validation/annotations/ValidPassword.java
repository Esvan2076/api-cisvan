package com.cisvan.api.validation.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import com.cisvan.api.validation.validators.PasswordValidator;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {
    
    String message() default "{InvalidPassword}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}