package com.cisvan.api.domain.users.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPromptStatusDTO {
    private Boolean hasAnsweredNotificationPrompt;
}