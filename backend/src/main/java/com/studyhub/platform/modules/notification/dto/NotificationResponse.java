package com.studyhub.platform.modules.notification.dto;

import com.studyhub.platform.common.enums.NotificationType;
import com.studyhub.platform.modules.notification.entity.Notification;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long userId,
        String title,
        String message,
        NotificationType type,
        String linkUrl,
        boolean isRead,
        Instant createdAt
) {
    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUser() != null ? notification.getUser().getId() : null,
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getLinkUrl(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
