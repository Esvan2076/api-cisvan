package com.cisvan.api.helper;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.ObjectError;

import com.cisvan.api.common.OperationResult;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidationHelper {

    private final MessageSource messageSource;

    public void addObjectError(String entityName, String errorCode, OperationResult operationResult) {
        // Obtener la traducción del nombre del campo en el idioma del usuario
        String translatedField = messageSource.getMessage("field." + entityName, null, LocaleContextHolder.getLocale());
    
        // Crear el error de objeto
        ObjectError objectError = new ObjectError(
            entityName, // Nombre de la entidad
            new String[]{errorCode}, // Código de error
            null, // Argumentos adicionales (no necesarios)
            null // Mensaje predeterminado (no se usa)
        );
    
        // Obtener el mensaje de error traducido
        String errorMessage = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());
    
        // Agregar el error con el nombre traducido del campo
        operationResult.addError(translatedField, errorMessage);
    }
    
    public void addObjectError(String entityName, String errorCode, String relatedEntity, OperationResult operationResult) {
        // Obtener la traducción del nombre del campo y la entidad relacionada en el idioma del usuario
        String translatedField = messageSource.getMessage("field." + entityName, null, LocaleContextHolder.getLocale());
        String translatedRelatedEntity = messageSource.getMessage("field." + relatedEntity, null, LocaleContextHolder.getLocale());
    
        // Crear el error de objeto
        ObjectError objectError = new ObjectError(
            entityName, // Nombre de la entidad principal
            new String[]{errorCode}, // Código de error
            new Object[]{translatedRelatedEntity}, // Pasamos la entidad relacionada traducida
            null // Mensaje predeterminado (no lo usamos)
        );
    
        // Obtener el mensaje de error traducido
        String errorMessage = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());
    
        // Agregar el error con el nombre traducido del campo
        operationResult.addError(translatedField, errorMessage);
    }

    public void addObjectError(String entityName, String errorCode, List<Long> numbers, OperationResult operationResult) {
        // Obtener la traducción del nombre del campo y la entidad relacionada en el idioma del usuario
        String translatedField = messageSource.getMessage("field." + entityName, null, LocaleContextHolder.getLocale());
    
        // Crear el error de objeto
        ObjectError objectError = new ObjectError(
            entityName, // Nombre de la entidad principal
            new String[]{errorCode}, // Código de error
            new Object[]{numbers}, // Pasamos la entidad relacionada traducida
            null // Mensaje predeterminado (no lo usamos)
        );
    
        // Obtener el mensaje de error traducido
        String errorMessage = messageSource.getMessage(objectError, LocaleContextHolder.getLocale());
    
        // Agregar el error con el nombre traducido del campo
        operationResult.addError(translatedField, errorMessage);
    }
}