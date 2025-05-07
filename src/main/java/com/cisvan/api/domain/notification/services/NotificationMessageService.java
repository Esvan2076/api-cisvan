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
        // Si no hay referencia, mandar null o string vacío
        Object[] args = referenceName != null ? new Object[]{referenceName} : new Object[]{""};
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
}