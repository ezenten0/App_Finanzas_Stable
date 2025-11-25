package com.example.notification.web.dto;

import com.example.notification.domain.NotificationChannel;
import com.example.notification.domain.NotificationStatus;
import java.time.Instant;

public record NotificationResponse(
        Long id,
        String recipient,
        NotificationChannel channel,
        String subject,
        String body,
        NotificationStatus status,
        Instant createdAt
) {
}
