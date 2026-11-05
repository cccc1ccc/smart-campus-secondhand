package com.must.secondhand.dto;

import com.must.secondhand.model.entity.Notification;
import com.must.secondhand.model.enums.NotificationType;

import java.time.Instant;

/** Notification entry shown in the bell dropdown and the notification page (REQ-8.1). */
public record NotificationView(
        Long id,
        NotificationType type,
        String title,
        String body,
        Long listingId,
        Long requestId,
        boolean read,
        Instant createdAt
) {

    public static NotificationView of(Notification notification) {
        return new NotificationView(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getListingId(),
                notification.getRequestId(),
                notification.isRead(),
                notification.getCreatedAt());
    }
}
