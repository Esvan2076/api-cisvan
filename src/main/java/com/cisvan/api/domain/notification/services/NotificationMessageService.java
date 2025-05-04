package com.cisvan.api.domain.notification.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.context.i18n.LocaleContextHolder;

@Service
@RequiredArgsConstructor
public class NotificationMessageService {

    private final MessageSource messageSource;

    public String resolveMessage(String code, String referenceName) {
        Object[] args = referenceName != null ? new Object[]{referenceName} : new Object[]{""};
        System.out.println("🛠️ Resolviendo mensaje para código: " + code + " con referencia: " + referenceName);
        
        try {
            String resolved = messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
            System.out.println("✅ Mensaje resuelto: " + resolved);
            return resolved;
        } catch (Exception e) {
            System.out.println("❌ Error al resolver mensaje para código: " + code + " → " + e.getMessage());
            return code; // fallback: devuelve el código si falla
        }
    }    
}