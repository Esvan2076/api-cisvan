package com.cisvan.api.helper;

import com.cisvan.api.common.OperationResult;

import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ControllerHelper {

    private final MessageSource messageSource;

    public OperationResult printAnnotationsErrors(BindingResult result) {
        List<OperationResult.ErrorDetail> errorDetails = new ArrayList<>();
        Locale locale = LocaleContextHolder.getLocale(); // Obtener el idioma actual
    
        // Manejar errores de campo
        result.getFieldErrors().forEach(err -> {
            String fullFieldName = err.getField(); // Ej: income.transactionId
    
            // Normalizar: quedarse solo con el último segmento del path
            String normalizedField = fullFieldName.contains(".")
                    ? fullFieldName.substring(fullFieldName.lastIndexOf('.') + 1)
                    : fullFieldName;
    
            String translatedField = messageSource.getMessage("field." + normalizedField, null, locale);
            String errorMessage = messageSource.getMessage(err, locale);
    
            errorDetails.add(
                OperationResult.ErrorDetail.builder()
                    .field(translatedField)
                    .message(errorMessage)
                    .build()
            );
        });
    
        // Manejar errores globales
        result.getGlobalErrors().forEach(err -> {
            String translatedField = messageSource.getMessage("field." + err.getObjectName(), null, locale);
            String errorMessage = messageSource.getMessage(err, locale);
    
            errorDetails.add(
                OperationResult.ErrorDetail.builder()
                    .field(translatedField)
                    .message(errorMessage)
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
