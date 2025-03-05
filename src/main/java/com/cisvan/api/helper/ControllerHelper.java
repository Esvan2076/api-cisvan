package com.cisvan.api.helper;

import com.cisvan.api.common.OperationResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ControllerHelper {

    @Autowired
    private MessageSource messageSource;

    public OperationResult printAnnotationsErrors(BindingResult result) {
        List<OperationResult.ErrorDetail> errorDetails = new ArrayList<>();
    
        // Manejar errores de campo
        result.getFieldErrors().forEach(err -> {
            String fieldName = err.getField();
            String errorMessage = messageSource.getMessage(err, null);
            errorDetails.add(
                OperationResult.ErrorDetail.builder().field(fieldName).message(errorMessage).build()
            );
        });
    
        // Manejar errores globales (de la entidad)
        result.getGlobalErrors().forEach(err -> {
            String errorMessage = messageSource.getMessage(err, null);
            errorDetails.add(
                OperationResult.ErrorDetail.builder().field(err.getObjectName()).message(errorMessage).build()
            );
        });
    
        return OperationResult.withErrors(errorDetails);
    }    

    public <T> ResponseEntity<T> handleOptional(Optional<T> optional) { 
        return optional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
