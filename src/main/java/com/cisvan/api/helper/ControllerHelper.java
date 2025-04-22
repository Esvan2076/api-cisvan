package com.cisvan.api.helper;

import com.cisvan.api.common.OperationResult;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ControllerHelper {

    public OperationResult printAnnotationsErrors(BindingResult result) {
        List<OperationResult.ErrorDetail> errorDetails = new ArrayList<>();
    
        // Errores de campo
        result.getFieldErrors().forEach(err -> {
            errorDetails.add(
                OperationResult.ErrorDetail.builder()
                    .field(err.getField()) // sin normalizar, tal cual lo manda el DTO
                    .message(err.getDefaultMessage()) // mensaje directo de la anotación
                    .build()
            );
        });
    
        // Errores globales (por si usas @Valid a nivel de clase)
        result.getGlobalErrors().forEach(err -> {
            errorDetails.add(
                OperationResult.ErrorDetail.builder()
                    .field(err.getObjectName())
                    .message(err.getDefaultMessage())
                    .build()
            );
        });
    
        return OperationResult.withErrors(errorDetails);
    }    

    public <T> ResponseEntity<T> handleOptional(Optional<T> optional) { 
        return optional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    public OperationResult validate(BindingResult result) {
        if (!result.hasErrors()) return new OperationResult();
        return printAnnotationsErrors(result);
    }    
}
